package pe.edu.upeu.epp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.CatalogoEppRequestDTO;
import pe.edu.upeu.epp.dto.request.CatalogoEppUpdateDTO;
import pe.edu.upeu.epp.dto.response.CatalogoEppResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para Catálogo EPP.
 * Implementa las operaciones CRUD y validaciones de negocio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoEppService {

    private final CatalogoEppRepository catalogoEppRepository;

    /**
     * Crear un nuevo EPP en el catálogo.
     */
    @Transactional
    public CatalogoEppResponseDTO crear(CatalogoEppRequestDTO request) {
        log.info("Creando nuevo EPP: {}", request.getNombreEpp());

        // Validar que el código de identificación sea único
        if (request.getCodigoIdentificacion() != null &&
                catalogoEppRepository.findByCodigoIdentificacion(request.getCodigoIdentificacion()).isPresent()) {
            throw new BusinessException("Ya existe un EPP con el código de identificación: " +
                    request.getCodigoIdentificacion());
        }

        // Crear entidad
        CatalogoEpp catalogoEpp = CatalogoEpp.builder()
                .nombreEpp(request.getNombreEpp())
                .codigoIdentificacion(request.getCodigoIdentificacion())
                .especificacionesTecnicas(request.getEspecificacionesTecnicas())
                .tipoUso(request.getTipoUso())
                .vidaUtilMeses(request.getVidaUtilMeses())
                .nivelProteccion(request.getNivelProteccion())
                .marca(request.getMarca())
                .unidadMedida(request.getUnidadMedida())
                .activo(true)
                .build();

        catalogoEpp = catalogoEppRepository.save(catalogoEpp);

        log.info("EPP creado exitosamente con ID: {}", catalogoEpp.getEppId());
        return mapToResponseDTO(catalogoEpp);
    }

    /**
     * Obtener un EPP por ID.
     */
    @Transactional(readOnly = true)
    public CatalogoEppResponseDTO obtenerPorId(Integer id) {
        log.debug("Buscando EPP con ID: {}", id);

        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));

        return mapToResponseDTO(catalogoEpp);
    }

    /**
     * Listar todos los EPPs con paginación.
     */
    @Transactional(readOnly = true)
    public Page<CatalogoEppResponseDTO> listarTodos(Pageable pageable) {
        log.debug("Listando todos los EPPs - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return catalogoEppRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Listar solo EPPs activos.
     */
    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> listarActivos() {
        log.debug("Listando EPPs activos");

        return catalogoEppRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar EPPs por nombre (búsqueda parcial).
     */
    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando EPPs con nombre que contiene: {}", nombre);

        return catalogoEppRepository.buscarPorNombreActivo(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar EPPs por tipo de uso.
     */
    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> listarPorTipo(CatalogoEpp.TipoUso tipoUso) {
        log.debug("Listando EPPs de tipo: {}", tipoUso);

        return catalogoEppRepository.findByTipoUso(tipoUso)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un EPP existente.
     */
    @Transactional
    public CatalogoEppResponseDTO actualizar(Integer id, CatalogoEppUpdateDTO request) {
        log.info("Actualizando EPP con ID: {}", id);

        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));

        // Actualizar solo los campos proporcionados (no null)
        if (request.getNombreEpp() != null) {
            catalogoEpp.setNombreEpp(request.getNombreEpp());
        }
        if (request.getEspecificacionesTecnicas() != null) {
            catalogoEpp.setEspecificacionesTecnicas(request.getEspecificacionesTecnicas());
        }
        if (request.getTipoUso() != null) {
            catalogoEpp.setTipoUso(request.getTipoUso());
        }
        if (request.getVidaUtilMeses() != null) {
            catalogoEpp.setVidaUtilMeses(request.getVidaUtilMeses());
        }
        if (request.getNivelProteccion() != null) {
            catalogoEpp.setNivelProteccion(request.getNivelProteccion());
        }
        if (request.getActivo() != null) {
            catalogoEpp.setActivo(request.getActivo());
        }
        if (request.getMarca() != null) {
            catalogoEpp.setMarca(request.getMarca());
        }
        if (request.getUnidadMedida() != null) {
            catalogoEpp.setUnidadMedida(request.getUnidadMedida());
        }

        catalogoEpp = catalogoEppRepository.save(catalogoEpp);

        log.info("EPP actualizado exitosamente: {}", id);
        return mapToResponseDTO(catalogoEpp);
    }

    /**
     * Eliminar (desactivar) un EPP.
     * No se elimina físicamente para mantener integridad referencial.
     */
    @Transactional
    public void eliminar(Integer id) {
        log.info("Desactivando EPP con ID: {}", id);

        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));

        // Verificar que no tenga inventario activo (opcional, comentado por ahora)
        // TODO: Implementar verificación de inventario antes de eliminar

        catalogoEpp.setActivo(false);
        catalogoEppRepository.save(catalogoEpp);

        log.info("EPP desactivado exitosamente: {}", id);
    }

    /**
     * Mapea una entidad CatalogoEpp a su DTO de respuesta.
     */
    private CatalogoEppResponseDTO mapToResponseDTO(CatalogoEpp catalogoEpp) {
        return CatalogoEppResponseDTO.builder()
                .eppId(catalogoEpp.getEppId())
                .nombreEpp(catalogoEpp.getNombreEpp())
                .codigoIdentificacion(catalogoEpp.getCodigoIdentificacion())
                .especificacionesTecnicas(catalogoEpp.getEspecificacionesTecnicas())
                .tipoUso(catalogoEpp.getTipoUso())
                .vidaUtilMeses(catalogoEpp.getVidaUtilMeses())
                .nivelProteccion(catalogoEpp.getNivelProteccion())
                .activo(catalogoEpp.getActivo())
                .marca(catalogoEpp.getMarca())
                .unidadMedida(catalogoEpp.getUnidadMedida())
                .fechaCreacion(catalogoEpp.getFechaCreacion())
                .fechaActualizacion(catalogoEpp.getFechaActualizacion())
                .build();
    }
}