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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioAreaService {

    private final InventarioAreaRepository inventarioAreaRepository;
    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final AreaRepository areaRepository;
    private final EstadoEppRepository estadoEppRepository;

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

        log.debug("Estado seleccionado: {} - Permite uso: {}", estado.getNombre(), estado.getPermiteUso());

        if (inventarioAreaRepository.findByEppIdAndAreaId(epp.getEppId(), area.getAreaId())
                .stream()
                .anyMatch(inv -> inv.getEstado().getEstadoId().equals(request.getEstadoId()))) {
            throw new BusinessException(
                    String.format("Ya existe un registro de inventario para EPP '%s' en área '%s' con estado '%s'",
                            epp.getNombreEpp(), area.getNombreArea(), estado.getNombre()));
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

        inventario = inventarioAreaRepository.save(inventario);

        log.info("Inventario de área creado exitosamente - ID: {}, Estado: {}",
                inventario.getInventarioAreaId(), estado.getNombre());

        return mapToResponseDTO(inventario);
    }

    @Transactional
    public InventarioAreaResponseDTO actualizar(Integer id, InventarioAreaUpdateDTO request) {
        log.info("Actualizando inventario de área ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario de área no encontrado con ID: " + id));

        if (request.getEstadoId() != null) {
            EstadoEpp nuevoEstado = estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

            log.info("Cambiando estado de '{}' a '{}' en área '{}'",
                    inventario.getEstado().getNombre(), nuevoEstado.getNombre(),
                    inventario.getArea().getNombreArea());

            inventario.setEstado(nuevoEstado);
        }

        if (request.getCantidadActual() != null) {
            inventario.setCantidadActual(request.getCantidadActual());
        }
        if (request.getCantidadMinima() != null) {
            inventario.setCantidadMinima(request.getCantidadMinima());
        }
        if (request.getCantidadMaxima() != null) {
            inventario.setCantidadMaxima(request.getCantidadMaxima());
        }
        if (request.getUbicacion() != null) {
            inventario.setUbicacion(request.getUbicacion());
        }

        inventario = inventarioAreaRepository.save(inventario);

        log.info("Inventario de área actualizado exitosamente: {}", id);

        return mapToResponseDTO(inventario);
    }

    @Transactional(readOnly = true)
    public InventarioAreaResponseDTO obtenerPorId(Integer id) {
        log.debug("Obteniendo inventario de área con ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario de área no encontrado con ID: " + id));

        return mapToResponseDTO(inventario);
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarPorArea(Integer areaId) {
        log.debug("Listando inventario del área ID: {}", areaId);

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + areaId));

        return inventarioAreaRepository.findByArea(area)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoPorArea(Integer areaId) {
        log.debug("Listando stock bajo del área ID: {}", areaId);

        return inventarioAreaRepository.findStockBajoPorArea(areaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockCritico() {
        log.debug("Listando todo el stock crítico");

        return inventarioAreaRepository.findAllStockCritico()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public InventarioAreaResponseDTO transferirStockCentralAArea(TransferenciaStockDTO request) {
        log.info("==== INICIANDO TRANSFERENCIA DE STOCK ====");
        log.info("EPP ID: {}, Área Destino ID: {}, Cantidad: {}",
                request.getEppId(), request.getAreaId(), request.getCantidad());

        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        Area areaDestino = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + request.getAreaId()));

        List<InventarioCentral> inventariosCentral = inventarioCentralRepository.findByEpp(epp)
                .stream()
                .filter(inv -> inv.getEstado().getPermiteUso() && inv.getCantidadActual() >= request.getCantidad())
                .toList();

        if (inventariosCentral.isEmpty()) {
            throw new BusinessException(
                    String.format("No hay stock suficiente disponible en inventario central para EPP '%s'. Solicitado: %d",
                            epp.getNombreEpp(), request.getCantidad()));
        }

        InventarioCentral inventarioCentral = inventariosCentral.get(0);
        EstadoEpp estado = inventarioCentral.getEstado();

        log.info("Usando inventario central ID: {}, Estado: {}, Stock disponible: {}",
                inventarioCentral.getInventarioCentralId(), estado.getNombre(),
                inventarioCentral.getCantidadActual());

        inventarioCentral.setCantidadActual(inventarioCentral.getCantidadActual() - request.getCantidad());
        inventarioCentralRepository.save(inventarioCentral);

        log.info("Stock restado de inventario central - Nueva cantidad: {}", inventarioCentral.getCantidadActual());

        InventarioArea inventarioArea = inventarioAreaRepository
                .findByEppIdAndAreaId(epp.getEppId(), areaDestino.getAreaId())
                .stream()
                .filter(inv -> inv.getEstado().getEstadoId().equals(estado.getEstadoId()))
                .findFirst()
                .orElseGet(() -> {
                    log.info("No existe inventario de área con estado '{}', creando nuevo registro", estado.getNombre());
                    return InventarioArea.builder()
                            .epp(epp)
                            .area(areaDestino)
                            .estado(estado)
                            .cantidadActual(0)
                            .cantidadMinima(5)
                            .cantidadMaxima(50)
                            .build();
                });

        inventarioArea.setCantidadActual(inventarioArea.getCantidadActual() + request.getCantidad());
        inventarioArea = inventarioAreaRepository.save(inventarioArea);

        log.info("Stock sumado a inventario de área - Nueva cantidad: {}", inventarioArea.getCantidadActual());
        log.info("==== TRANSFERENCIA COMPLETADA EXITOSAMENTE ====");

        return mapToResponseDTO(inventarioArea);
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando inventario de área ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario de área no encontrado con ID: " + id));

        if (inventario.getCantidadActual() > 0) {
            throw new BusinessException(
                    "No se puede eliminar un inventario con stock disponible. " +
                            "Cantidad actual: " + inventario.getCantidadActual());
        }

        inventarioAreaRepository.delete(inventario);

        log.info("Inventario de área eliminado exitosamente: {}", id);
    }

    private InventarioAreaResponseDTO mapToResponseDTO(InventarioArea inventario) {
        Integer porcentajeStock = inventario.calcularPorcentajeStock();

        return InventarioAreaResponseDTO.builder()
                .inventarioAreaId(inventario.getInventarioAreaId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(inventario.getEpp().getCodigoIdentificacion())
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
                .porcentajeStock(porcentajeStock)
                .build();
    }
}