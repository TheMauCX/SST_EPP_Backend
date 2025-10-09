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
import pe.edu.upeu.epp.entity.InventarioCentral;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.InventarioCentralRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para Inventario Central.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioCentralService {

    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;

    /**
     * Registrar nuevo stock en inventario central.
     */
    @Transactional
    public InventarioCentralResponseDTO crear(InventarioCentralRequestDTO request) {
        log.info("Registrando nuevo stock en inventario central - EPP ID: {}, Lote: {}",
                request.getEppId(), request.getLote());

        // Validar que el EPP exista
        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        // Validar que no exista un registro con el mismo EPP y lote
        if (inventarioCentralRepository.findByEppAndLote(catalogoEpp, request.getLote()).isPresent()) {
            throw new BusinessException("Ya existe un registro de inventario para el EPP " +
                    catalogoEpp.getNombreEpp() + " con el lote: " + request.getLote());
        }

        // Validar cantidades
        if (request.getCantidadMaxima() != null && request.getCantidadMaxima() < request.getCantidadMinima()) {
            throw new BusinessException("La cantidad máxima no puede ser menor a la cantidad mínima");
        }

        // Crear entidad
        InventarioCentral inventario = InventarioCentral.builder()
                .epp(catalogoEpp)
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

        log.info("Stock registrado exitosamente - ID: {}, Cantidad: {}",
                inventario.getInventarioCentralId(), inventario.getCantidadActual());

        return mapToResponseDTO(inventario);
    }

    /**
     * Obtener inventario por ID.
     */
    @Transactional(readOnly = true)
    public InventarioCentralResponseDTO obtenerPorId(Integer id) {
        log.debug("Buscando inventario central con ID: {}", id);

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        return mapToResponseDTO(inventario);
    }

    /**
     * Listar todo el inventario central.
     */
    @Transactional(readOnly = true)
    public Page<InventarioCentralResponseDTO> listarTodo(Pageable pageable) {
        log.debug("Listando inventario central - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return inventarioCentralRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Listar inventario por EPP.
     */
    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarPorEpp(Integer eppId) {
        log.debug("Listando inventario del EPP: {}", eppId);

        return inventarioCentralRepository.findByEppId(eppId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar stock bajo (cantidad actual <= cantidad mínima).
     */
    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarStockBajo() {
        log.debug("Listando inventario con stock bajo");

        return inventarioCentralRepository.findStockBajo()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar EPPs próximos a vencer (dentro de 30 días).
     */
    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarProximosAVencer() {
        log.debug("Listando inventario próximo a vencer");

        LocalDate fechaLimite = LocalDate.now().plusDays(30);

        return inventarioCentralRepository.findProximosAVencer(fechaLimite)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar información del inventario.
     */
    @Transactional
    public InventarioCentralResponseDTO actualizar(Integer id, InventarioCentralUpdateDTO request) {
        log.info("Actualizando inventario central con ID: {}", id);

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        // Actualizar solo los campos proporcionados
        if (request.getCantidadActual() != null) {
            inventario.setCantidadActual(request.getCantidadActual());
        }

        if (request.getCantidadMinima() != null) {
            inventario.setCantidadMinima(request.getCantidadMinima());
        }

        if (request.getCantidadMaxima() != null) {
            // Validar que sea mayor a la mínima
            if (inventario.getCantidadMinima() != null &&
                    request.getCantidadMaxima() < inventario.getCantidadMinima()) {
                throw new BusinessException("La cantidad máxima no puede ser menor a la cantidad mínima");
            }
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

    /**
     * Ajustar stock del inventario (ingreso o salida manual).
     */
    @Transactional
    public InventarioCentralResponseDTO ajustarStock(Integer id, AjusteInventarioDTO request) {
        log.info("Ajustando stock del inventario {} - Ajuste: {} - Motivo: {}",
                id, request.getCantidadAjuste(), request.getMotivo());

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        int nuevaCantidad = inventario.getCantidadActual() + request.getCantidadAjuste();

        // Validar que no quede en negativo
        if (nuevaCantidad < 0) {
            throw new BusinessException("El ajuste resultaría en una cantidad negativa. " +
                    "Stock actual: " + inventario.getCantidadActual() +
                    ", Ajuste solicitado: " + request.getCantidadAjuste());
        }

        inventario.setCantidadActual(nuevaCantidad);

        // Agregar observación sobre el ajuste
        String nuevaObservacion = String.format("[%s] Ajuste de %+d unidades. Motivo: %s",
                LocalDate.now(), request.getCantidadAjuste(), request.getMotivo());

        if (inventario.getObservaciones() != null && !inventario.getObservaciones().isEmpty()) {
            inventario.setObservaciones(inventario.getObservaciones() + "\n" + nuevaObservacion);
        } else {
            inventario.setObservaciones(nuevaObservacion);
        }

        inventario = inventarioCentralRepository.save(inventario);

        log.info("Stock ajustado exitosamente. Nueva cantidad: {}", inventario.getCantidadActual());
        return mapToResponseDTO(inventario);
    }

    /**
     * Eliminar registro de inventario (solo si cantidad es 0).
     */
    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando inventario central con ID: {}", id);

        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        // Validar que no tenga stock
        if (inventario.getCantidadActual() > 0) {
            throw new BusinessException("No se puede eliminar un inventario con stock disponible. " +
                    "Cantidad actual: " + inventario.getCantidadActual());
        }

        inventarioCentralRepository.delete(inventario);

        log.info("Inventario eliminado exitosamente: {}", id);
    }

    /**
     * Mapea una entidad InventarioCentral a su DTO de respuesta.
     */
    private InventarioCentralResponseDTO mapToResponseDTO(InventarioCentral inventario) {
        // Calcular días para vencer
        Integer diasParaVencer = null;
        if (inventario.getFechaVencimiento() != null) {
            diasParaVencer = (int) ChronoUnit.DAYS.between(LocalDate.now(), inventario.getFechaVencimiento());
        }

        return InventarioCentralResponseDTO.builder()
                .inventarioCentralId(inventario.getInventarioCentralId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(inventario.getEpp().getCodigoIdentificacion())
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