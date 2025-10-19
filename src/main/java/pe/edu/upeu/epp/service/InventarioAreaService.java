package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.InventarioAreaRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioAreaUpdateDTO;
import pe.edu.upeu.epp.dto.request.TransferenciaStockDTO;
import pe.edu.upeu.epp.dto.response.InventarioAreaResponseDTO;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InventarioArea;
import pe.edu.upeu.epp.entity.InventarioCentral;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.AreaRepository;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.EstadoEppRepository;
import pe.edu.upeu.epp.repository.InventarioAreaRepository;
import pe.edu.upeu.epp.repository.InventarioCentralRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Inventario por Área con lógica de merge.
 *
 * @author Sistema EPP
 * @version 2.0 - Corregido para usar inventarioAreaId
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

    /**
     * Crea un nuevo inventario de área.
     */
    @Transactional
    public InventarioAreaResponseDTO crear(InventarioAreaRequestDTO request) {
        log.info("Creando inventario de área - EPP ID: {}, Área ID: {}, Estado ID: {}",
                request.getEppId(), request.getAreaId(), request.getEstadoId());

        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + request.getAreaId()));

        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        boolean existe = inventarioAreaRepository.existsByEppAndAreaAndEstado(epp, area, estado);
        if (existe) {
            throw new BusinessException(
                    String.format("Ya existe inventario para EPP '%s' en área '%s' con estado '%s'",
                            epp.getNombreEpp(), area.getNombreArea(), estado.getNombre())
            );
        }

        InventarioArea inventario = InventarioArea.builder()
                .epp(epp)
                .area(area)
                .estado(estado)
                .cantidadActual(request.getCantidadActual())
                .cantidadMinima(request.getCantidadMinima())
                .cantidadMaxima(request.getCantidadMaxima())
                .ubicacion(request.getUbicacion())
                .build();

        InventarioArea guardado = inventarioAreaRepository.save(inventario);
        log.info("Inventario de área creado con ID: {}", guardado.getInventarioAreaId());

        return mapToResponseDTO(guardado);
    }

    /**
     * Actualiza un inventario de área con lógica de MERGE.
     */
    @Transactional
    public InventarioAreaResponseDTO actualizar(Integer id, InventarioAreaUpdateDTO request) {
        log.info("Actualizando inventario de área ID: {}", id);

        InventarioArea inventarioActual = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        CatalogoEpp nuevoEpp = inventarioActual.getEpp();
        Area nuevaArea = inventarioActual.getArea();

        EstadoEpp nuevoEstado = inventarioActual.getEstado();
        if (request.getEstadoId() != null) {
            nuevoEstado = estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));
        }

        boolean cambioEstado = !nuevoEstado.getEstadoId().equals(inventarioActual.getEstado().getEstadoId());

        log.debug("Cambio de estado detectado: {}", cambioEstado);

        if (cambioEstado) {
            log.info("Detectado cambio de estado. Buscando registro existente...");

            Optional<InventarioArea> registroExistente =
                    inventarioAreaRepository.findByEppAndAreaAndEstado(nuevoEpp, nuevaArea, nuevoEstado);

            if (registroExistente.isPresent() && !registroExistente.get().getInventarioAreaId().equals(id)) {
                log.warn("Ya existe un registro con la nueva combinación. Ejecutando MERGE...");

                InventarioArea destino = registroExistente.get();
                Integer cantidadAMover = inventarioActual.getCantidadActual();

                log.info("MERGE: Moviendo {} unidades del registro ID {} al registro ID {}",
                        cantidadAMover, id, destino.getInventarioAreaId());

                destino.setCantidadActual(destino.getCantidadActual() + cantidadAMover);

                if (request.getCantidadMinima() != null) {
                    destino.setCantidadMinima(request.getCantidadMinima());
                }
                if (request.getCantidadMaxima() != null) {
                    destino.setCantidadMaxima(request.getCantidadMaxima());
                }
                if (request.getUbicacion() != null) {
                    destino.setUbicacion(request.getUbicacion());
                }

                InventarioArea consolidado = inventarioAreaRepository.save(destino);
                inventarioAreaRepository.delete(inventarioActual);

                log.info("MERGE completado. Registro {} eliminado. Nuevo total en {}: {}",
                        id, consolidado.getInventarioAreaId(), consolidado.getCantidadActual());

                return mapToResponseDTO(consolidado);
            }
        }

        log.info("No hay conflictos. Actualizando normalmente...");

        if (request.getEstadoId() != null) {
            inventarioActual.setEstado(nuevoEstado);
        }
        if (request.getCantidadMinima() != null) {
            inventarioActual.setCantidadMinima(request.getCantidadMinima());
        }
        if (request.getCantidadMaxima() != null) {
            inventarioActual.setCantidadMaxima(request.getCantidadMaxima());
        }
        if (request.getUbicacion() != null) {
            inventarioActual.setUbicacion(request.getUbicacion());
        }

        InventarioArea actualizado = inventarioAreaRepository.save(inventarioActual);
        log.info("Inventario de área actualizado. ID: {}", actualizado.getInventarioAreaId());

        return mapToResponseDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public InventarioAreaResponseDTO obtenerPorId(Integer id) {
        log.info("Obteniendo inventario de área ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        return mapToResponseDTO(inventario);
    }

    @Transactional(readOnly = true)
    public Page<InventarioAreaResponseDTO> listarTodos(Pageable pageable) {
        log.info("Listando inventarios de área");
        return inventarioAreaRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarPorArea(Integer areaId) {
        log.info("Listando inventarios del área ID: {}", areaId);

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + areaId));

        return inventarioAreaRepository.findByArea(area).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoPorArea(Integer areaId) {
        log.info("Obteniendo alertas de stock bajo para área ID: {}", areaId);

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + areaId));

        return inventarioAreaRepository.findStockBajoByArea(area).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoGlobal() {
        log.info("Obteniendo alertas de stock bajo globales");
        return inventarioAreaRepository.findStockBajo().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Transferir stock de Central a Área.
     */
    @Transactional
    public InventarioAreaResponseDTO transferirStockCentralAArea(TransferenciaStockDTO request) {
        log.info("Iniciando transferencia - EPP ID: {}, Área ID: {}, Cantidad: {}",
                request.getEppId(), request.getAreaId(), request.getCantidad());

        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + request.getAreaId()));

        List<InventarioCentral> inventariosCentrales =
                inventarioCentralRepository.findByEppAndEstadoPermiteUsoOrderByCantidadDesc(epp, true);

        InventarioCentral invCentral = inventariosCentrales.stream()
                .filter(inv -> inv.getCantidadDisponible() >= request.getCantidad())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        String.format("No hay stock suficiente en inventario central. EPP: '%s', Solicitado: %d",
                                epp.getNombreEpp(), request.getCantidad())
                ));

        log.info("Inventario central encontrado - ID: {}, Estado: {}, Disponible: {}",
                invCentral.getInventarioCentralId(),
                invCentral.getEstado().getNombre(),
                invCentral.getCantidadDisponible());

        if (invCentral.getCantidadDisponible() < request.getCantidad()) {
            throw new BusinessException(
                    String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                            invCentral.getCantidadDisponible(), request.getCantidad())
            );
        }

        invCentral.setCantidadActual(invCentral.getCantidadActual() - request.getCantidad());
        inventarioCentralRepository.save(invCentral);

        log.info("Stock reducido en central. Nueva cantidad: {}", invCentral.getCantidadActual());

        EstadoEpp estado = invCentral.getEstado();
        Optional<InventarioArea> invAreaOpt =
                inventarioAreaRepository.findByEppAndAreaAndEstado(epp, area, estado);

        InventarioArea invArea;
        if (invAreaOpt.isPresent()) {
            invArea = invAreaOpt.get();
            invArea.setCantidadActual(invArea.getCantidadActual() + request.getCantidad());
            log.info("Inventario de área actualizado. Nueva cantidad: {}", invArea.getCantidadActual());
        } else {
            invArea = InventarioArea.builder()
                    .epp(epp)
                    .area(area)
                    .estado(estado)
                    .cantidadActual(request.getCantidad())
                    .cantidadMinima(5)
                    .cantidadMaxima(request.getCantidad() * 2)
                    .ubicacion("Transferido desde central")
                    .build();
            log.info("Nuevo inventario de área creado con {} unidades", request.getCantidad());
        }

        InventarioArea guardado = inventarioAreaRepository.save(invArea);
        log.info("Transferencia completada. Inventario área ID: {}", guardado.getInventarioAreaId());

        return mapToResponseDTO(guardado);
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando inventario de área ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        if (inventario.getCantidadActual() > 0) {
            throw new BusinessException("No se puede eliminar inventario con stock disponible");
        }

        inventarioAreaRepository.delete(inventario);
    }

    private InventarioAreaResponseDTO mapToResponseDTO(InventarioArea inventario) {
        return InventarioAreaResponseDTO.builder()
                .inventarioAreaId(inventario.getInventarioAreaId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(inventario.getEpp().getCodigoIdentificacion())
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