package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.dto.request.CatalogoEppRequestDTO;
import pe.edu.upeu.epp.dto.request.CatalogoEppUpdateDTO;
import pe.edu.upeu.epp.dto.response.CatalogoEppResponseDTO;
import pe.edu.upeu.epp.dto.response.CatalogoTallaResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.CatalogoTalla;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.CatalogoTallaRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoEppService {

    private final CatalogoEppRepository catalogoEppRepository;
    private final CatalogoTallaRepository tallaRepository;
    private final AzureStorageService azureStorageService;

    // ── Crear ────────────────────────────────────────────────────────────

    @Transactional
    public CatalogoEppResponseDTO crear(CatalogoEppRequestDTO request, MultipartFile fotoArchivo) {
        log.info("Creando nuevo EPP: {}", request.getNombreEpp());

        String urlFoto = resolverFoto(fotoArchivo, request.getFotoReferencia(), null);
        Set<CatalogoTalla> tallas = resolverTallas(request.getTallaIds());

        CatalogoEpp epp = CatalogoEpp.builder()
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
                .fotoReferencia(urlFoto)
                .color(request.getColor())
                .tallasDisponibles(tallas)
                .activo(true)
                .build();

        return mapToResponseDTO(catalogoEppRepository.save(epp));
    }

    // ── Actualizar ───────────────────────────────────────────────────────

    @Transactional
    public CatalogoEppResponseDTO actualizar(Integer id, CatalogoEppUpdateDTO request, MultipartFile fotoArchivo) {
        log.info("Actualizando EPP con ID: {}", id);

        CatalogoEpp epp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));

        // Foto: si viene archivo nuevo, subir; si viene URL, usar; si no viene nada, mantener la actual
        String urlFoto = resolverFoto(fotoArchivo, request.getFotoReferencia(), epp.getFotoReferencia());
        epp.setFotoReferencia(urlFoto);

        if (request.getNombreEpp() != null)                 epp.setNombreEpp(request.getNombreEpp());
        if (request.getTipoUso() != null)                   epp.setTipoUso(request.getTipoUso());
        if (request.getAprobacionesNormas() != null)        epp.setAprobacionesNormas(request.getAprobacionesNormas());
        if (request.getCaracteristicas() != null)           epp.setCaracteristicas(request.getCaracteristicas());
        if (request.getFabricante() != null)                epp.setFabricante(request.getFabricante());
        if (request.getTiempoUsoFabricante() != null)       epp.setTiempoUsoFabricante(request.getTiempoUsoFabricante());
        if (request.getTiempoUsoOperacion() != null)        epp.setTiempoUsoOperacion(request.getTiempoUsoOperacion());
        if (request.getCondicionesMantenimiento() != null)  epp.setCondicionesMantenimiento(request.getCondicionesMantenimiento());
        if (request.getCondicionesAlmacenamiento() != null) epp.setCondicionesAlmacenamiento(request.getCondicionesAlmacenamiento());
        if (request.getCondicionesCambioPrematuro() != null)epp.setCondicionesCambioPrematuro(request.getCondicionesCambioPrematuro());
        if (request.getActivo() != null)                    epp.setActivo(request.getActivo());
        if (request.getColor() != null)                     epp.setColor(request.getColor());

        // Tallas: si vienen en el request, reemplazar el set completo
        if (request.getTallaIds() != null) {
            epp.setTallasDisponibles(resolverTallas(request.getTallaIds()));
        }

        return mapToResponseDTO(catalogoEppRepository.save(epp));
    }

    // ── HU-19: Subir ficha técnica PDF ───────────────────────────────────

    /**
     * Recibe un PDF y lo sube a Azure en la carpeta epps/fichas/.
     * Actualiza el campo fichaTecnicaPath en la entidad.
     */
    @Transactional
    public CatalogoEppResponseDTO subirFichaTecnica(Integer eppId, MultipartFile pdfFile) {
        log.info("Subiendo ficha técnica para EPP ID: {}", eppId);

        CatalogoEpp epp = catalogoEppRepository.findById(eppId)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + eppId));

        String urlPdf = azureStorageService.subirFichaTecnica(pdfFile, eppId);
        epp.setFichaTecnicaPath(urlPdf);

        log.info("Ficha técnica subida: {}", urlPdf);
        return mapToResponseDTO(catalogoEppRepository.save(epp));
    }

    // ── HU-23: Filtros en catálogo ───────────────────────────────────────

    /**
     * Lista EPPs con filtro por rotación.
     * rotacion = "alta"  → EPPs con más de 0 entregas en los últimos 90 días
     * rotacion = "nula"  → EPPs sin ninguna entrega en los últimos 90 días
     * rotacion = null    → todos (comportamiento anterior)
     */
    @Transactional(readOnly = true)
    public Page<CatalogoEppResponseDTO> listarTodos(Pageable pageable, String rotacion) {
        if (rotacion == null || rotacion.isBlank()) {
            return catalogoEppRepository.findAll(pageable).map(this::mapToResponseDTO);
        }
        LocalDateTime desde = LocalDateTime.now().minusDays(90);
        return switch (rotacion.toLowerCase()) {
            case "alta" -> catalogoEppRepository.findConRotacionAlta(desde, pageable).map(this::mapToResponseDTO);
            case "nula" -> catalogoEppRepository.findSinRotacion(desde, pageable).map(this::mapToResponseDTO);
            default     -> catalogoEppRepository.findAll(pageable).map(this::mapToResponseDTO);
        };
    }

    // ── Consultas básicas (sin cambios) ──────────────────────────────────

    @Transactional(readOnly = true)
    public CatalogoEppResponseDTO obtenerPorId(Integer id) {
        return mapToResponseDTO(catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<CatalogoEppResponseDTO> listarTodos(Pageable pageable) {
        return listarTodos(pageable, null);
    }

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> listarActivos() {
        return catalogoEppRepository.findByActivoTrue().stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> buscarPorNombre(String nombre) {
        return catalogoEppRepository.buscarPorNombreActivo(nombre).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> listarPorTipo(CatalogoEpp.TipoUso tipoUso) {
        return catalogoEppRepository.findByTipoUso(tipoUso).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Integer id) {
        CatalogoEpp epp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));
        epp.setActivo(false);
        catalogoEppRepository.save(epp);
    }

    // ── Helpers privados ─────────────────────────────────────────────────

    /**
     * Resuelve la URL de la foto con prioridad:
     * 1. Archivo multipart nuevo → subir a Azure
     * 2. URL texto en request → usar directamente
     * 3. URL anterior → mantener
     */
    private String resolverFoto(MultipartFile archivo, String urlRequest, String urlActual) {
        if (archivo != null && !archivo.isEmpty()) {
            return azureStorageService.subirArchivo(archivo); // método genérico legacy
        }
        if (urlRequest != null && !urlRequest.isBlank()) {
            return urlRequest;
        }
        return urlActual;
    }

    private Set<CatalogoTalla> resolverTallas(Set<Integer> tallaIds) {
        if (tallaIds == null || tallaIds.isEmpty()) return new HashSet<>();
        Set<CatalogoTalla> tallas = new HashSet<>();
        for (Integer tallaId : tallaIds) {
            CatalogoTalla talla = tallaRepository.findById(tallaId)
                    .orElseThrow(() -> new BusinessException("Talla no encontrada con ID: " + tallaId));
            tallas.add(talla);
        }
        return tallas;
    }

    // ── Mapeo ────────────────────────────────────────────────────────────

    private CatalogoEppResponseDTO mapToResponseDTO(CatalogoEpp epp) {
        Set<CatalogoTallaResponseDTO> tallasDTO = epp.getTallasDisponibles() == null
                ? new HashSet<>()
                : epp.getTallasDisponibles().stream()
                    .map(t -> CatalogoTallaResponseDTO.builder()
                            .tallaId(t.getTallaId())
                            .nombre(t.getNombre())
                            .descripcion(t.getDescripcion())
                            .ordenVisualizacion(t.getOrdenVisualizacion())
                            .build())
                    .collect(Collectors.toSet());

        return CatalogoEppResponseDTO.builder()
                .eppId(epp.getEppId())
                .nombreEpp(epp.getNombreEpp())
                .tipoUso(epp.getTipoUso())
                .aprobacionesNormas(epp.getAprobacionesNormas())
                .caracteristicas(epp.getCaracteristicas())
                .fabricante(epp.getFabricante())
                .tiempoUsoFabricante(epp.getTiempoUsoFabricante())
                .tiempoUsoOperacion(epp.getTiempoUsoOperacion())
                .condicionesMantenimiento(epp.getCondicionesMantenimiento())
                .condicionesAlmacenamiento(epp.getCondicionesAlmacenamiento())
                .condicionesCambioPrematuro(epp.getCondicionesCambioPrematuro())
                .fotoReferencia(epp.getFotoReferencia())
                .color(epp.getColor())
                .tallasDisponibles(tallasDTO)
                .fichaTecnicaPath(epp.getFichaTecnicaPath())
                .activo(epp.getActivo())
                .fechaCreacion(epp.getFechaCreacion())
                .fechaActualizacion(epp.getFechaActualizacion())
                .build();
    }
}
