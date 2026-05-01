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

        // Crear entidad
        CatalogoEpp catalogoEpp = CatalogoEpp.builder()
                .nombreEpp(request.getNombreEpp())
                .tipoUso(request.getTipoUso())
                .aprobacionesNormas(request.getAprobacionesNormas())
                .caracteristicas(request.getCaracteristicas())
                .fabricante(request.getFabricante())
                .tiempoUsoFabricante(request.getTiempoUsoFabricante())
                .tiempoUsoOperacion(request.getTiempoUsoOperacion())
                .condicionesMantenimiento(request.getCondicionesMantenimiento())
                .condicionesAlmacenamiento(request.getCondicionesAlmacenamiento())
                .condicionesCambioPrematuro(request.getCondicionesCambioPrematuro())
                .fotoReferencia(request.getFotoReferencia())
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
    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> buscarPorFabricante(String fabricante) {
        return catalogoEppRepository.findByFabricante(fabricante).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> buscarPorNorma(String norma) {
        return catalogoEppRepository.buscarPorNorma(norma).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
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
        if (request.getTipoUso() != null) {
            catalogoEpp.setTipoUso(request.getTipoUso());
        }
        if (request.getAprobacionesNormas() != null) {
            catalogoEpp.setAprobacionesNormas(request.getAprobacionesNormas());
        }
        if (request.getCaracteristicas() != null) {
            catalogoEpp.setCaracteristicas(request.getCaracteristicas());
        }
        if (request.getFabricante() != null) {
            catalogoEpp.setFabricante(request.getFabricante());
        }
        if (request.getTiempoUsoFabricante() != null) {
            catalogoEpp.setTiempoUsoFabricante(request.getTiempoUsoFabricante());
        }
        if (request.getTiempoUsoOperacion() != null) {
            catalogoEpp.setTiempoUsoOperacion(request.getTiempoUsoOperacion());
        }
        if (request.getCondicionesMantenimiento() != null) {
            catalogoEpp.setCondicionesMantenimiento(request.getCondicionesMantenimiento());
        }
        if (request.getCondicionesAlmacenamiento() != null) {
            catalogoEpp.setCondicionesAlmacenamiento(request.getCondicionesAlmacenamiento());
        }
        if (request.getCondicionesCambioPrematuro() != null) {
            catalogoEpp.setCondicionesCambioPrematuro(request.getCondicionesCambioPrematuro());
        }
        if (request.getFotoReferencia() != null) {
            catalogoEpp.setFotoReferencia(request.getFotoReferencia());
        }
        if (request.getActivo() != null) {
            catalogoEpp.setActivo(request.getActivo());
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

        // TODO: Implementar verificación de inventario antes de eliminar (Validar en InventarioCentralRepository si hay stock)

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
                .tipoUso(catalogoEpp.getTipoUso())
                .aprobacionesNormas(catalogoEpp.getAprobacionesNormas())
                .caracteristicas(catalogoEpp.getCaracteristicas())
                .fabricante(catalogoEpp.getFabricante())
                .tiempoUsoFabricante(catalogoEpp.getTiempoUsoFabricante())
                .tiempoUsoOperacion(catalogoEpp.getTiempoUsoOperacion())
                .condicionesMantenimiento(catalogoEpp.getCondicionesMantenimiento())
                .condicionesAlmacenamiento(catalogoEpp.getCondicionesAlmacenamiento())
                .condicionesCambioPrematuro(catalogoEpp.getCondicionesCambioPrematuro())
                .fotoReferencia(catalogoEpp.getFotoReferencia())
                .activo(catalogoEpp.getActivo())
                .fechaCreacion(catalogoEpp.getFechaCreacion())
                .fechaActualizacion(catalogoEpp.getFechaActualizacion())
                .build();
    }
}