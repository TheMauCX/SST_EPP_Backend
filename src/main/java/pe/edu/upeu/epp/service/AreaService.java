package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.AreaRequestDTO;
import pe.edu.upeu.epp.dto.request.AreaUpdateDTO;
import pe.edu.upeu.epp.dto.response.AreaResponseDTO;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.Trabajador;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.AreaRepository;
import pe.edu.upeu.epp.repository.TrabajadorRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para Áreas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AreaService {

    private final AreaRepository areaRepository;
    private final TrabajadorRepository trabajadorRepository;

    /**
     * Crear una nueva área.
     */
    @Transactional
    public AreaResponseDTO crear(AreaRequestDTO request) {
        log.info("Creando nueva área: {}", request.getNombreArea());

        // Validar que el nombre sea único
        if (areaRepository.findByNombreArea(request.getNombreArea()).isPresent()) {
            throw new BusinessException("Ya existe un área con el nombre: " + request.getNombreArea());
        }

        // Validar que el código sea único (si se proporciona)
        if (request.getCodigoArea() != null &&
                areaRepository.findByCodigoArea(request.getCodigoArea()).isPresent()) {
            throw new BusinessException("Ya existe un área con el código: " + request.getCodigoArea());
        }

        // Validar responsable si se proporciona
        if (request.getResponsableId() != null) {
            if (!trabajadorRepository.existsById(request.getResponsableId())) {
                throw new BusinessException("El trabajador responsable no existe");
            }
        }

        // Crear entidad
        Area area = Area.builder()
                .nombreArea(request.getNombreArea())
                .codigoArea(request.getCodigoArea())
                .descripcion(request.getDescripcion())
                .ubicacion(request.getUbicacion())
                .responsableId(request.getResponsableId())
                .activo(true)
                .build();

        area = areaRepository.save(area);

        log.info("Área creada exitosamente con ID: {}", area.getAreaId());
        return mapToResponseDTO(area);
    }

    /**
     * Obtener un área por ID.
     */
    @Transactional(readOnly = true)
    public AreaResponseDTO obtenerPorId(Integer id) {
        log.debug("Buscando área con ID: {}", id);

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + id));

        return mapToResponseDTO(area);
    }

    /**
     * Listar todas las áreas con paginación.
     */
    @Transactional(readOnly = true)
    public Page<AreaResponseDTO> listarTodas(Pageable pageable) {
        log.debug("Listando todas las áreas - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return areaRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Listar solo áreas activas.
     */
    @Transactional(readOnly = true)
    public List<AreaResponseDTO> listarActivas() {
        log.debug("Listando áreas activas");

        return areaRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un área existente.
     */
    @Transactional
    public AreaResponseDTO actualizar(Integer id, AreaUpdateDTO request) {
        log.info("Actualizando área con ID: {}", id);

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + id));

        // Actualizar solo los campos proporcionados
        if (request.getNombreArea() != null) {
            // Validar que el nuevo nombre no esté en uso por otra área
            areaRepository.findByNombreArea(request.getNombreArea())
                    .ifPresent(existente -> {
                        if (!existente.getAreaId().equals(id)) {
                            throw new BusinessException("Ya existe un área con ese nombre");
                        }
                    });
            area.setNombreArea(request.getNombreArea());
        }

        if (request.getDescripcion() != null) {
            area.setDescripcion(request.getDescripcion());
        }

        if (request.getUbicacion() != null) {
            area.setUbicacion(request.getUbicacion());
        }

        if (request.getResponsableId() != null) {
            // Validar que el responsable exista
            if (!trabajadorRepository.existsById(request.getResponsableId())) {
                throw new BusinessException("El trabajador responsable no existe");
            }
            area.setResponsableId(request.getResponsableId());
        }

        if (request.getActivo() != null) {
            area.setActivo(request.getActivo());
        }

        area = areaRepository.save(area);

        log.info("Área actualizada exitosamente: {}", id);
        return mapToResponseDTO(area);
    }

    /**
     * Eliminar (desactivar) un área.
     */
    @Transactional
    public void eliminar(Integer id) {
        log.info("Desactivando área con ID: {}", id);

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + id));

        // Verificar que no tenga trabajadores activos
        long trabajadoresActivos = trabajadorRepository.countByAreaAndEstado(area, Trabajador.EstadoTrabajador.ACTIVO);
        if (trabajadoresActivos > 0) {
            throw new BusinessException(
                    "No se puede desactivar el área porque tiene " + trabajadoresActivos + " trabajadores activos");
        }

        area.setActivo(false);
        areaRepository.save(area);

        log.info("Área desactivada exitosamente: {}", id);
    }

    /**
     * Mapea una entidad Area a su DTO de respuesta.
     */
    private AreaResponseDTO mapToResponseDTO(Area area) {
        String responsableNombre = null;

        if (area.getResponsableId() != null) {
            responsableNombre = trabajadorRepository.findById(area.getResponsableId())
                    .map(t -> t.getNombres() + " " + t.getApellidos())
                    .orElse(null);
        }

        return AreaResponseDTO.builder()
                .areaId(area.getAreaId())
                .nombreArea(area.getNombreArea())
                .codigoArea(area.getCodigoArea())
                .descripcion(area.getDescripcion())
                .ubicacion(area.getUbicacion())
                .responsableId(area.getResponsableId())
                .responsableNombre(responsableNombre)
                .activo(area.getActivo())
                .fechaCreacion(area.getFechaCreacion())
                .build();
    }
}