package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.AjusteInventarioDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralUpdateDTO;
import pe.edu.upeu.epp.dto.response.InventarioCentralResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InventarioCentral;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.EstadoEppRepository;
import pe.edu.upeu.epp.repository.InventarioCentralRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioCentralService {

    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final EstadoEppRepository estadoEppRepository;

    @Transactional
    public InventarioCentralResponseDTO crear(InventarioCentralRequestDTO request) {
        log.info("Registrando nuevo stock en inventario central - EPP ID: {}, Lote: {}, Estado ID: {}",
                request.getEppId(), request.getLote(), request.getEstadoId());

        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        log.debug("Estado seleccionado: {} - Permite uso: {}", estado.getNombre(), estado.getPermiteUso());

        if (!Boolean.TRUE.equals(estado.getPermiteUso())) {
            log.warn("Se está registrando stock con estado que no permite uso: {}", estado.getNombre());
        }

        inventarioCentralRepository.findByEppAndLote(epp, request.getLote())
                .stream()
                .filter(inv -> inv.getEstado().getEstadoId().equals(request.getEstadoId()))
                .findFirst()
                .ifPresent(inv -> {
                    throw new BusinessException(
                            String.format("Ya existe un registro de inventario para EPP '%s', lote '%s' y estado '%s'",
                                    epp.getNombreEpp(), request.getLote(), estado.getNombre()));
                });

        InventarioCentral inventario = InventarioCentral.builder()
                .epp(epp)
                .estado(estado)
                .cantidadActual(request.getCantidadActual())
                .cantidadMinima(request.getCantidadMinima())
                .cantidadMaxima(request.getCantidadMaxima())
                .ubicacionBodega(request.getUbicacionBodega())
                .lote(request.getLote())
                .fechaAdquisicion(request.getFechaAdquisicion())
                .costoUnitario(request.getCostoUnitario())
                .proveedor(request.getProveedor())
                .fechaVencimiento(request.getFechaVencimiento())
                .observaciones(request.getObservaciones())
                .build();

        inventario = inventarioCentralRepository.save(inventario);

        log.info("Stock registrado exitosamente - ID: {}, Estado: {}",
                inventario.getInventarioCentralId(), estado.getNombre());

        return mapToResponseDTO(inventario);
    }

    @Transactional
    public InventarioCentralResponseDTO actualizar(Integer id, InventarioCentralUpdateDTO request) {
        log.info("Actualizando inventario central ID: {}", id);

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        if (request.getEstadoId() != null) {
            EstadoEpp nuevoEstado = estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

            log.info("Cambiando estado de '{}' a '{}'",
                    inventario.getEstado().getNombre(), nuevoEstado.getNombre());

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
        if (request.getUbicacionBodega() != null) {
            inventario.setUbicacionBodega(request.getUbicacionBodega());
        }
        if (request.getCostoUnitario() != null) {
            inventario.setCostoUnitario(request.getCostoUnitario());
        }
        if (request.getProveedor() != null) {
            inventario.setProveedor(request.getProveedor());
        }
        if (request.getFechaVencimiento() != null) {
            inventario.setFechaVencimiento(request.getFechaVencimiento());
        }
        if (request.getObservaciones() != null) {
            inventario.setObservaciones(request.getObservaciones());
        }

        inventario = inventarioCentralRepository.save(inventario);

        log.info("Inventario actualizado exitosamente: {}", id);

        return mapToResponseDTO(inventario);
    }

    @Transactional(readOnly = true)
    public InventarioCentralResponseDTO obtenerPorId(Integer id) {
        log.debug("Obteniendo inventario central con ID: {}", id);

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        return mapToResponseDTO(inventario);
    }

    @Transactional(readOnly = true)
    public Page<InventarioCentralResponseDTO> listarTodo(Pageable pageable) {
        log.debug("Listando todo el inventario central - Página: {}", pageable.getPageNumber());

        return inventarioCentralRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarPorEpp(Integer eppId) {
        log.debug("Listando inventario por EPP ID: {}", eppId);

        CatalogoEpp epp = catalogoEppRepository.findById(eppId)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + eppId));

        return inventarioCentralRepository.findByEpp(epp)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarStockBajo() {
        log.debug("Listando stock bajo");

        return inventarioCentralRepository.findStockBajo()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarProximosAVencer() {
        log.debug("Listando EPPs próximos a vencer");

        LocalDate fechaLimite = LocalDate.now().plusDays(30);

        return inventarioCentralRepository.findProximosAVencer(fechaLimite)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventarioCentralResponseDTO ajustarStock(Integer id, AjusteInventarioDTO request) {
        log.info("Ajustando stock del inventario ID: {} - Cantidad: {}, Motivo: {}",
                id, request.getCantidadAjuste(), request.getMotivo());

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        int nuevaCantidad = inventario.getCantidadActual() + request.getCantidadAjuste();

        if (nuevaCantidad < 0) {
            throw new BusinessException(
                    String.format("El ajuste resultaría en cantidad negativa. Actual: %d, Ajuste: %d",
                            inventario.getCantidadActual(), request.getCantidadAjuste()));
        }

        inventario.setCantidadActual(nuevaCantidad);

        String tipoAjuste = request.getCantidadAjuste() > 0 ? "INGRESO" : "SALIDA";
        String observacionAjuste = String.format("%s - %s: %s",
                tipoAjuste, request.getMotivo(),
                inventario.getObservaciones() != null ? inventario.getObservaciones() : "");

        inventario.setObservaciones(observacionAjuste.trim());

        inventario = inventarioCentralRepository.save(inventario);

        log.info("Stock ajustado exitosamente - Nueva cantidad: {}", nuevaCantidad);

        return mapToResponseDTO(inventario);
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando inventario central ID: {}", id);

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        if (inventario.getCantidadActual() > 0) {
            throw new BusinessException(
                    "No se puede eliminar un inventario con stock disponible. " +
                            "Cantidad actual: " + inventario.getCantidadActual());
        }

        inventarioCentralRepository.delete(inventario);

        log.info("Inventario eliminado exitosamente: {}", id);
    }

    private InventarioCentralResponseDTO mapToResponseDTO(InventarioCentral inventario) {
        Integer diasParaVencer = null;
        if (inventario.getFechaVencimiento() != null) {
            diasParaVencer = (int) ChronoUnit.DAYS.between(LocalDate.now(), inventario.getFechaVencimiento());
        }

        return InventarioCentralResponseDTO.builder()
                .inventarioCentralId(inventario.getInventarioCentralId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(inventario.getEpp().getCodigoIdentificacion())
                .estadoId(inventario.getEstado().getEstadoId())
                .estadoNombre(inventario.getEstado().getNombre())
                .estadoDescripcion(inventario.getEstado().getDescripcion())
                .estadoPermiteUso(inventario.getEstado().getPermiteUso())
                .estadoColorHex(inventario.getEstado().getColorHex())
                .cantidadActual(inventario.getCantidadActual())
                .cantidadMinima(inventario.getCantidadMinima())
                .cantidadMaxima(inventario.getCantidadMaxima())
                .ubicacionBodega(inventario.getUbicacionBodega())
                .lote(inventario.getLote())
                .fechaAdquisicion(inventario.getFechaAdquisicion())
                .costoUnitario(inventario.getCostoUnitario())
                .proveedor(inventario.getProveedor())
                .fechaVencimiento(inventario.getFechaVencimiento())
                .observaciones(inventario.getObservaciones())
                .ultimaActualizacion(inventario.getUltimaActualizacion())
                .necesitaReposicion(inventario.necesitaReposicion())
                .diasParaVencer(diasParaVencer)
                .build();
    }
}