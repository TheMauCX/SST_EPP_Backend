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
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.InventarioArea;
import pe.edu.upeu.epp.entity.InventarioCentral;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.AreaRepository;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.InventarioAreaRepository;
import pe.edu.upeu.epp.repository.InventarioCentralRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para Inventario por Área.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioAreaService {

    private final InventarioAreaRepository inventarioAreaRepository;
    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final AreaRepository areaRepository;

    /**
     * Crear registro de inventario para un área.
     */
    @Transactional
    public InventarioAreaResponseDTO crear(InventarioAreaRequestDTO request) {
        log.info("Creando inventario de área - EPP ID: {}, Área ID: {}",
                request.getEppId(), request.getAreaId());

        // Validar que el EPP exista
        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        // Validar que el área exista
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + request.getAreaId()));

        // Validar que no exista ya un registro para ese EPP en esa área
        if (inventarioAreaRepository.findByEppIdAndAreaId(request.getEppId(), request.getAreaId()).isPresent()) {
            throw new BusinessException("Ya existe un registro de inventario para el EPP " +
                    catalogoEpp.getNombreEpp() + " en el área " + area.getNombreArea());
        }

        // Validar cantidades
        if (request.getCantidadMaxima() != null && request.getCantidadMaxima() < request.getCantidadMinima()) {
            throw new BusinessException("La cantidad máxima no puede ser menor a la cantidad mínima");
        }

        // Crear entidad
        InventarioArea inventario = InventarioArea.builder()
                .epp(catalogoEpp)
                .area(area)
                .cantidadActual(request.getCantidadActual())
                .cantidadMinima(request.getCantidadMinima())
                .cantidadMaxima(request.getCantidadMaxima())
                .ubicacion(request.getUbicacion())
                .build();

        inventario = inventarioAreaRepository.save(inventario);

        log.info("Inventario de área creado exitosamente - ID: {}", inventario.getInventarioAreaId());
        return mapToResponseDTO(inventario);
    }

    /**
     * Obtener inventario por ID.
     */
    @Transactional(readOnly = true)
    public InventarioAreaResponseDTO obtenerPorId(Integer id) {
        log.debug("Buscando inventario de área con ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario de área no encontrado con ID: " + id));

        return mapToResponseDTO(inventario);
    }

    /**
     * Listar inventario de un área específica.
     */
    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarPorArea(Integer areaId) {
        log.debug("Listando inventario del área: {}", areaId);

        return inventarioAreaRepository.findByAreaId(areaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar stock bajo de un área.
     */
    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoPorArea(Integer areaId) {
        log.debug("Listando stock bajo del área: {}", areaId);

        return inventarioAreaRepository.findStockBajoPorArea(areaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar todo el stock crítico (todas las áreas).
     */
    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockCritico() {
        log.debug("Listando todo el stock crítico");

        return inventarioAreaRepository.findAllStockCritico()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar inventario de área.
     */
    @Transactional
    public InventarioAreaResponseDTO actualizar(Integer id, InventarioAreaUpdateDTO request) {
        log.info("Actualizando inventario de área con ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario de área no encontrado con ID: " + id));

        // Actualizar solo los campos proporcionados
        if (request.getCantidadActual() != null) {
            inventario.setCantidadActual(request.getCantidadActual());
        }

        if (request.getCantidadMinima() != null) {
            inventario.setCantidadMinima(request.getCantidadMinima());
        }

        if (request.getCantidadMaxima() != null) {
            if (inventario.getCantidadMinima() != null &&
                    request.getCantidadMaxima() < inventario.getCantidadMinima()) {
                throw new BusinessException("La cantidad máxima no puede ser menor a la cantidad mínima");
            }
            inventario.setCantidadMaxima(request.getCantidadMaxima());
        }

        if (request.getUbicacion() != null) {
            inventario.setUbicacion(request.getUbicacion());
        }

        inventario = inventarioAreaRepository.save(inventario);

        log.info("Inventario de área actualizado exitosamente: {}", id);
        return mapToResponseDTO(inventario);
    }

    /**
     * Transferir stock desde inventario central a un área.
     * TRANSACCIÓN ATÓMICA CRÍTICA.
     */
    @Transactional(rollbackFor = Exception.class)
    public InventarioAreaResponseDTO transferirStockCentralAArea(TransferenciaStockDTO request) {
        log.info("Iniciando transferencia de stock - Central ID: {}, Área: {}, Cantidad: {}",
                request.getInventarioCentralId(), request.getAreaDestinoId(), request.getCantidad());

        // 1. VALIDAR INVENTARIO CENTRAL
        InventarioCentral inventarioCentral = inventarioCentralRepository
                .findById(request.getInventarioCentralId())
                .orElseThrow(() -> new EntityNotFoundException("Inventario central no encontrado"));

        // 2. VALIDAR STOCK SUFICIENTE
        if (inventarioCentral.getCantidadActual() < request.getCantidad()) {
            throw new BusinessException(
                    String.format("Stock insuficiente en inventario central. Disponible: %d, Solicitado: %d",
                            inventarioCentral.getCantidadActual(), request.getCantidad())
            );
        }

        // 3. VALIDAR ÁREA DESTINO
        Area areaDestino = areaRepository.findById(request.getAreaDestinoId())
                .orElseThrow(() -> new EntityNotFoundException("Área destino no encontrada"));

        // 4. RESTAR STOCK DEL INVENTARIO CENTRAL
        inventarioCentral.setCantidadActual(inventarioCentral.getCantidadActual() - request.getCantidad());
        inventarioCentralRepository.save(inventarioCentral);

        log.debug("Stock restado del inventario central. Nueva cantidad: {}",
                inventarioCentral.getCantidadActual());

        // 5. BUSCAR O CREAR INVENTARIO EN EL ÁREA
        InventarioArea inventarioArea = inventarioAreaRepository
                .findByEppIdAndAreaId(inventarioCentral.getEpp().getEppId(), request.getAreaDestinoId())
                .orElseGet(() -> {
                    log.info("Creando nuevo registro de inventario para el área {}", areaDestino.getNombreArea());
                    return InventarioArea.builder()
                            .epp(inventarioCentral.getEpp())
                            .area(areaDestino)
                            .cantidadActual(0)
                            .cantidadMinima(5) // Valor por defecto
                            .ubicacion("Almacén " + areaDestino.getNombreArea())
                            .build();
                });

        // 6. SUMAR STOCK AL INVENTARIO DEL ÁREA
        inventarioArea.setCantidadActual(inventarioArea.getCantidadActual() + request.getCantidad());
        inventarioArea = inventarioAreaRepository.save(inventarioArea);

        log.info("Transferencia completada exitosamente. Nueva cantidad en área: {}",
                inventarioArea.getCantidadActual());

        return mapToResponseDTO(inventarioArea);
    }

    /**
     * Eliminar inventario de área (solo si cantidad es 0).
     */
    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando inventario de área con ID: {}", id);

        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario de área no encontrado con ID: " + id));

        if (inventario.getCantidadActual() > 0) {
            throw new BusinessException("No se puede eliminar un inventario con stock disponible. " +
                    "Cantidad actual: " + inventario.getCantidadActual());
        }

        inventarioAreaRepository.delete(inventario);

        log.info("Inventario de área eliminado exitosamente: {}", id);
    }

    /**
     * Mapea una entidad InventarioArea a su DTO de respuesta.
     */
    private InventarioAreaResponseDTO mapToResponseDTO(InventarioArea inventario) {
        // Calcular porcentaje de stock
        Integer porcentajeStock = null;
        if (inventario.getCantidadMaxima() != null && inventario.getCantidadMaxima() > 0) {
            porcentajeStock = (inventario.getCantidadActual() * 100) / inventario.getCantidadMaxima();
        }

        return InventarioAreaResponseDTO.builder()
                .inventarioAreaId(inventario.getInventarioAreaId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(inventario.getEpp().getCodigoIdentificacion())
                .areaId(inventario.getArea().getAreaId())
                .areaNombre(inventario.getArea().getNombreArea())
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