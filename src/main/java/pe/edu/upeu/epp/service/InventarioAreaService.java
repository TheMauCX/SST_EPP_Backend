package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.InventarioAreaRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioAreaUpdateDTO;
import pe.edu.upeu.epp.dto.request.TransferenciaStockDTO;
import pe.edu.upeu.epp.dto.response.InventarioAreaResponseDTO;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de Inventario por Área.
 *
 * HU-22: Transferencia refactorizada para garantizar atomicidad real.
 * La operación de resta en central y suma en área ocurre dentro de una
 * única transacción gestionada por Spring. Si cualquiera de los dos saves
 * falla, Hibernate hace rollback de ambos automáticamente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioAreaService {

    private final InventarioAreaRepository inventarioAreaRepository;
    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final AreaRepository areaRepository;
    private final EstadoEppRepository estadoEppRepository;

    // =====================================================================
    // HU-22: TRANSFERENCIA ATÓMICA CENTRAL → ÁREA (REFACTORIZADA)
    // =====================================================================

    /**
     * Transfiere stock desde el inventario central a un área.
     *
     * CORRECCIÓN HU-22: La versión anterior hacía save() del central
     * ANTES de guardar el área, dejando la BD en estado inconsistente
     * si el segundo save fallaba. Ahora ambas operaciones ocurren dentro
     * de la misma transacción y ningún save se ejecuta hasta que ambos
     * objetos están listos. Spring/Hibernate hace flush al final del
     * método o ante el primer fallo hace rollback completo.
     */
    @Transactional
    public InventarioAreaResponseDTO transferirStockCentralAArea(TransferenciaStockDTO request) {
        log.info("Iniciando transferencia: EPP ID={}, Área ID={}, Cantidad={}",
                request.getEppId(), request.getAreaId(), request.getCantidad());

        // 1. Cargar entidades — cualquier EntityNotFoundException aborta antes de tocar stock
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "EPP no encontrado con ID: " + request.getEppId()));

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Área no encontrada con ID: " + request.getAreaId()));

        // 2. Buscar lote disponible en central con stock suficiente
        //    Se filtra directamente en la query (permiteUso=true) y se ordena
        //    por cantidad desc para consumir primero el lote con más stock.
        InventarioCentral invCentral = inventarioCentralRepository
                .findByEppAndEstadoPermiteUsoOrderByCantidadDesc(epp, true)
                .stream()
                .filter(inv -> inv.getCantidadDisponible() >= request.getCantidad())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        String.format("Stock insuficiente en inventario central para '%s'. " +
                                        "Solicitado: %d. Verifique disponibilidad.",
                                epp.getNombreEpp(), request.getCantidad())));

        // 3. Calcular nuevas cantidades ANTES de hacer ningún save
        int nuevaCantidadCentral = invCentral.getCantidadActual() - request.getCantidad();

        // Guardia extra: aunque la query ya filtra, esta validación protege
        // ante condiciones de carrera en entornos con alta concurrencia
        if (nuevaCantidadCentral < 0) {
            throw new BusinessException(
                    "Transferencia rechazada: la cantidad resultante en central sería negativa.");
        }

        EstadoEpp estado = invCentral.getEstado();

        // 4. Preparar inventario de área (buscar existente o construir nuevo)
        Optional<InventarioArea> invAreaOpt =
                inventarioAreaRepository.findByEppAndAreaAndEstado(epp, area, estado);

        InventarioArea invArea;
        if (invAreaOpt.isPresent()) {
            invArea = invAreaOpt.get();
            invArea.setCantidadActual(invArea.getCantidadActual() + request.getCantidad());
            log.info("Actualizando inventario de área existente ID={}", invArea.getInventarioAreaId());
        } else {
            invArea = InventarioArea.builder()
                    .epp(epp)
                    .area(area)
                    .estado(estado)
                    .cantidadActual(request.getCantidad())
                    .cantidadMinima(5)
                    .cantidadMaxima(request.getCantidad() * 2)
                    .ubicacion("Almacén " + area.getNombreArea())
                    .build();
            log.info("Creando nuevo registro de inventario en área '{}'", area.getNombreArea());
        }

        // 5. Aplicar el descuento en central
        invCentral.setCantidadActual(nuevaCantidadCentral);

        // 6. Persistir AMBOS dentro de la misma transacción.
        //    Si inventarioAreaRepository.save() lanza una excepción,
        //    Spring revertirá también el cambio en invCentral (rollback completo).
        inventarioCentralRepository.save(invCentral);
        InventarioArea invAreaGuardada = inventarioAreaRepository.save(invArea);

        log.info("Transferencia completada exitosamente. Central restante: {}, Área nueva cantidad: {}",
                nuevaCantidadCentral, invAreaGuardada.getCantidadActual());

        return mapToResponseDTO(invAreaGuardada);
    }

    // =====================================================================
    // CRUD ESTÁNDAR (sin cambios funcionales respecto al sprint anterior)
    // =====================================================================

    @Transactional
    public InventarioAreaResponseDTO crear(InventarioAreaRequestDTO request) {
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + request.getAreaId()));
        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        if (inventarioAreaRepository.existsByEppAndAreaAndEstado(epp, area, estado)) {
            throw new BusinessException("Ya existe inventario para este EPP, Área y Estado.");
        }

        InventarioArea inventario = InventarioArea.builder()
                .epp(epp).area(area).estado(estado)
                .cantidadActual(request.getCantidadActual())
                .cantidadMinima(request.getCantidadMinima())
                .cantidadMaxima(request.getCantidadMaxima())
                .ubicacion(request.getUbicacion())
                .build();

        return mapToResponseDTO(inventarioAreaRepository.save(inventario));
    }

    @Transactional
    public InventarioAreaResponseDTO actualizar(Integer id, InventarioAreaUpdateDTO request) {
        InventarioArea inventarioActual = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        EstadoEpp nuevoEstado = inventarioActual.getEstado();
        if (request.getEstadoId() != null) {
            nuevoEstado = estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado"));
        }

        if (!nuevoEstado.getEstadoId().equals(inventarioActual.getEstado().getEstadoId())) {
            Optional<InventarioArea> registroExistente = inventarioAreaRepository
                    .findByEppAndAreaAndEstado(inventarioActual.getEpp(), inventarioActual.getArea(), nuevoEstado);

            if (registroExistente.isPresent() && !registroExistente.get().getInventarioAreaId().equals(id)) {
                InventarioArea destino = registroExistente.get();
                destino.setCantidadActual(destino.getCantidadActual() + inventarioActual.getCantidadActual());
                if (request.getCantidadMinima() != null) destino.setCantidadMinima(request.getCantidadMinima());
                if (request.getCantidadMaxima() != null) destino.setCantidadMaxima(request.getCantidadMaxima());
                if (request.getUbicacion() != null) destino.setUbicacion(request.getUbicacion());
                InventarioArea consolidado = inventarioAreaRepository.save(destino);
                inventarioAreaRepository.delete(inventarioActual);
                return mapToResponseDTO(consolidado);
            }
        }

        if (request.getEstadoId() != null) inventarioActual.setEstado(nuevoEstado);
        if (request.getCantidadMinima() != null) inventarioActual.setCantidadMinima(request.getCantidadMinima());
        if (request.getCantidadMaxima() != null) inventarioActual.setCantidadMaxima(request.getCantidadMaxima());
        if (request.getUbicacion() != null) inventarioActual.setUbicacion(request.getUbicacion());

        return mapToResponseDTO(inventarioAreaRepository.save(inventarioActual));
    }

    @Transactional(readOnly = true)
    public InventarioAreaResponseDTO obtenerPorId(Integer id) {
        return mapToResponseDTO(inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id)));
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarPorArea(Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + areaId));
        return inventarioAreaRepository.findByArea(area).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoPorArea(Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + areaId));
        return inventarioAreaRepository.findStockBajoByArea(area).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoGlobal() {
        return inventarioAreaRepository.findStockBajo().stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Integer id) {
        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));
        if (inventario.getCantidadActual() > 0) {
            throw new BusinessException("No se puede eliminar inventario con stock disponible.");
        }
        inventarioAreaRepository.delete(inventario);
    }

    // =====================================================================
    // MAPEO
    // =====================================================================

    private InventarioAreaResponseDTO mapToResponseDTO(InventarioArea inventario) {
        return InventarioAreaResponseDTO.builder()
                .inventarioAreaId(inventario.getInventarioAreaId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(null)
                .tipoUso(inventario.getEpp().getTipoUso())
                .areaId(inventario.getArea().getAreaId())
                .areaNombre(inventario.getArea().getNombreArea())
                .estadoId(inventario.getEstado().getEstadoId())
                .estadoNombre(inventario.getEstado().getNombre())
                .estadoDescripcion(inventario.getEstado().getDescripcion())
                .estadoPermiteUso(inventario.getEstado().getPermiteUso())
                .estadoColorHex(inventario.getEstado().getColorHex())
                .cantidadActual(inventario.getCantidadActual())
                .cantidadMinima(inventario.getCantidadMinima())
                .cantidadMaxima(inventario.getCantidadMaxima())
                .ubicacion(inventario.getUbicacion())
                .ultimaActualizacion(inventario.getUltimaActualizacion())
                .necesitaReposicion(inventario.necesitaReposicion())
                .porcentajeStock(inventario.calcularPorcentajeStock())
                .build();
    }
}
