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
 * Servicio de Inventario por Área.
 *
 * Fix (tallas):
 *   La transferencia ahora usa el tallaId del DTO para:
 *   1. Filtrar el lote correcto en inventario central (EPP + talla específica)
 *   2. Crear/actualizar el registro de área con esa misma talla
 *
 *   Sin esto, si el central tiene 30 cascos-M y 20 cascos-L, transferir
 *   "15 cascos" podría descontar del lote incorrecto.
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
    private final CatalogoTallaRepository tallaRepository;

    // ── Transferencia atómica Central → Área (corregida para tallas) ─────

    @Transactional
    public InventarioAreaResponseDTO transferirStockCentralAArea(TransferenciaStockDTO request) {
        log.info("Transferencia: EPP={}, Área={}, Cantidad={}, Talla={}",
                request.getEppId(), request.getAreaId(),
                request.getCantidad(), request.getTallaId());

        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "EPP no encontrado con ID: " + request.getEppId()));

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Área no encontrada con ID: " + request.getAreaId()));

        // Resolver la talla solicitada (puede ser null)
        CatalogoTalla tallaRequerida = null;
        if (request.getTallaId() != null) {
            tallaRequerida = tallaRepository.findById(request.getTallaId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Talla no encontrada con ID: " + request.getTallaId()));
        }

        final CatalogoTalla tallaFinal = tallaRequerida;

        // Buscar lote en central con stock suficiente del EPP + talla correcta
        InventarioCentral invCentral;
        if (tallaFinal != null) {
            // Buscar específicamente lotes con esa talla
            invCentral = inventarioCentralRepository
                    .findByEppAndEstadoPermiteUsoOrderByCantidadDesc(epp, true)
                    .stream()
                    .filter(inv -> tallaFinal.equals(inv.getTalla()))
                    .filter(inv -> inv.getCantidadDisponible() >= request.getCantidad())
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(String.format(
                            "Stock insuficiente en central para '%s' talla '%s'. " +
                            "Solicitado: %d. Verifique disponibilidad.",
                            epp.getNombreEpp(), tallaFinal.getNombre(), request.getCantidad())));
        } else {
            // Buscar lotes sin talla
            invCentral = inventarioCentralRepository
                    .findByEppAndEstadoPermiteUsoOrderByCantidadDesc(epp, true)
                    .stream()
                    .filter(inv -> inv.getTalla() == null)
                    .filter(inv -> inv.getCantidadDisponible() >= request.getCantidad())
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(String.format(
                            "Stock insuficiente en central para '%s' (sin talla). " +
                            "Solicitado: %d. Verifique disponibilidad.",
                            epp.getNombreEpp(), request.getCantidad())));
        }

        // Guardia ante negativos
        int nuevaCantidadCentral = invCentral.getCantidadActual() - request.getCantidad();
        if (nuevaCantidadCentral < 0) {
            throw new BusinessException("Transferencia rechazada: cantidad central resultante sería negativa.");
        }

        EstadoEpp estado = invCentral.getEstado();

        // Buscar o crear registro de área (EPP + área + estado + talla)
        Optional<InventarioArea> invAreaOpt;
        if (tallaFinal != null) {
            invAreaOpt = inventarioAreaRepository
                    .findByEppAndAreaAndEstadoAndTalla(epp, area, estado, tallaFinal);
        } else {
            invAreaOpt = inventarioAreaRepository
                    .findByEppAndAreaAndEstadoSinTalla(epp, area, estado);
        }

        InventarioArea invArea;
        if (invAreaOpt.isPresent()) {
            invArea = invAreaOpt.get();
            invArea.setCantidadActual(invArea.getCantidadActual() + request.getCantidad());
            log.info("Sumando a registro de área existente ID={}", invArea.getInventarioAreaId());
        } else {
            invArea = InventarioArea.builder()
                    .epp(epp)
                    .area(area)
                    .estado(estado)
                    .talla(tallaFinal)
                    .cantidadActual(request.getCantidad())
                    .cantidadMinima(5)
                    .cantidadMaxima(request.getCantidad() * 2)
                    .ubicacion("Almacén " + area.getNombreArea())
                    .build();
            log.info("Creando registro nuevo en área '{}' para talla '{}'",
                    area.getNombreArea(), tallaFinal != null ? tallaFinal.getNombre() : "sin talla");
        }

        // Persistir ambos en la misma transacción (atómica)
        invCentral.setCantidadActual(nuevaCantidadCentral);
        inventarioCentralRepository.save(invCentral);
        InventarioArea guardada = inventarioAreaRepository.save(invArea);

        log.info("Transferencia completada. Central restante: {}, Área ahora tiene: {}",
                nuevaCantidadCentral, guardada.getCantidadActual());

        return mapToResponseDTO(guardada);
    }

    // ── CRUD estándar ─────────────────────────────────────────────────────

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
        if (inventario.getCantidadActual() > 0)
            throw new BusinessException("No se puede eliminar inventario con stock disponible.");
        inventarioAreaRepository.delete(inventario);
    }

    // ── Mapeo (ahora incluye talla) ───────────────────────────────────────

    private InventarioAreaResponseDTO mapToResponseDTO(InventarioArea inv) {
        return InventarioAreaResponseDTO.builder()
                .inventarioAreaId(inv.getInventarioAreaId())
                .eppId(inv.getEpp().getEppId())
                .eppNombre(inv.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(null)
                .tipoUso(inv.getEpp().getTipoUso())
                .areaId(inv.getArea().getAreaId())
                .areaNombre(inv.getArea().getNombreArea())
                .tallaId(inv.getTalla() != null ? inv.getTalla().getTallaId() : null)
                .tallaNombre(inv.getTalla() != null ? inv.getTalla().getNombre() : null)
                .estadoId(inv.getEstado().getEstadoId())
                .estadoNombre(inv.getEstado().getNombre())
                .estadoDescripcion(inv.getEstado().getDescripcion())
                .estadoPermiteUso(inv.getEstado().getPermiteUso())
                .estadoColorHex(inv.getEstado().getColorHex())
                .cantidadActual(inv.getCantidadActual())
                .cantidadMinima(inv.getCantidadMinima())
                .cantidadMaxima(inv.getCantidadMaxima())
                .ubicacion(inv.getUbicacion())
                .ultimaActualizacion(inv.getUltimaActualizacion())
                .necesitaReposicion(inv.necesitaReposicion())
                .porcentajeStock(inv.calcularPorcentajeStock())
                .build();
    }
}
