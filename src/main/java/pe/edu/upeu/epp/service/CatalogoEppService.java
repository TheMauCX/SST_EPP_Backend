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
import pe.edu.upeu.epp.dto.response.NormaEppResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.CatalogoTalla;
import pe.edu.upeu.epp.entity.NormaEpp;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.CatalogoTallaRepository;
import pe.edu.upeu.epp.repository.NormaEppRepository;

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
    private final NormaEppRepository normaEppRepository;
    private final AzureStorageService azureStorageService;
    private final NormaEppService normaEppService;

    @Transactional
    public CatalogoEppResponseDTO crear(CatalogoEppRequestDTO request, MultipartFile fotoArchivo) {
        String urlFoto = null;
        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            urlFoto = azureStorageService.subirFotoEpp(fotoArchivo, 0);
        }

        Set<CatalogoTalla> tallas = resolverTallas(request.getTallaIds());
        Set<NormaEpp> normas = resolverNormas(request.getNormaIds());

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
                .fotoReferencia(urlFoto != null ? urlFoto : request.getFotoReferencia())
                .color(request.getColor())
                .cantidadMinima(request.getCantidadMinima() != null ? request.getCantidadMinima() : 0)
                .cantidadMaxima(request.getCantidadMaxima())
                .tallasDisponibles(tallas)
                .normасAplicables(normas)
                .activo(true)
                .build();

        CatalogoEpp saved = catalogoEppRepository.save(epp);

        // Si se subió la foto con ID temporal 0, actualizar con el ID real
        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            String urlFotoReal = azureStorageService.subirFotoEpp(fotoArchivo, saved.getEppId());
            saved.setFotoReferencia(urlFotoReal);
            saved = catalogoEppRepository.save(saved);
        }

        return toResponseDTO(saved);
    }

    @Transactional
    public CatalogoEppResponseDTO actualizar(Integer id, CatalogoEppUpdateDTO request, MultipartFile fotoArchivo) {
        CatalogoEpp epp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + id));

        if (request.getNombreEpp()             != null) epp.setNombreEpp(request.getNombreEpp());
        if (request.getTipoUso()               != null) epp.setTipoUso(request.getTipoUso());
        if (request.getAprobacionesNormas()    != null) epp.setAprobacionesNormas(request.getAprobacionesNormas());
        if (request.getCaracteristicas()       != null) epp.setCaracteristicas(request.getCaracteristicas());
        if (request.getFabricante()            != null) epp.setFabricante(request.getFabricante());
        if (request.getTiempoUsoFabricante()   != null) epp.setTiempoUsoFabricante(request.getTiempoUsoFabricante());
        if (request.getTiempoUsoOperacion()    != null) epp.setTiempoUsoOperacion(request.getTiempoUsoOperacion());
        if (request.getCondicionesMantenimiento()   != null) epp.setCondicionesMantenimiento(request.getCondicionesMantenimiento());
        if (request.getCondicionesAlmacenamiento()  != null) epp.setCondicionesAlmacenamiento(request.getCondicionesAlmacenamiento());
        if (request.getCondicionesCambioPrematuro()  != null) epp.setCondicionesCambioPrematuro(request.getCondicionesCambioPrematuro());
        if (request.getFotoReferencia()        != null) epp.setFotoReferencia(request.getFotoReferencia());
        if (request.getColor()                 != null) epp.setColor(request.getColor());
        if (request.getActivo()                != null) epp.setActivo(request.getActivo());
        if (request.getCantidadMinima()        != null) epp.setCantidadMinima(request.getCantidadMinima());
        if (request.getCantidadMaxima()        != null) epp.setCantidadMaxima(request.getCantidadMaxima());

        if (request.getTallaIds() != null)
            epp.setTallasDisponibles(resolverTallas(request.getTallaIds()));

        // Normas: null = no modificar, Set vacío = eliminar todas
        if (request.getNormaIds() != null)
            epp.setNormасAplicables(resolverNormas(request.getNormaIds()));

        if (fotoArchivo != null && !fotoArchivo.isEmpty())
            epp.setFotoReferencia(azureStorageService.subirFotoEpp(fotoArchivo, id));

        return toResponseDTO(catalogoEppRepository.save(epp));
    }

    @Transactional
    public CatalogoEppResponseDTO subirFichaTecnica(Integer id, MultipartFile file) {
        CatalogoEpp epp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + id));
        epp.setFichaTecnicaPath(azureStorageService.subirFichaTecnica(file, id));
        return toResponseDTO(catalogoEppRepository.save(epp));
    }

    @Transactional(readOnly = true)
    public Page<CatalogoEppResponseDTO> listar(Pageable pageable, String rotacion) {
        java.time.LocalDateTime desde = java.time.LocalDateTime.now().minusDays(90);
        if ("alta".equalsIgnoreCase(rotacion))
            return catalogoEppRepository.findConRotacionAlta(desde, pageable).map(this::toResponseDTO);
        if ("nula".equalsIgnoreCase(rotacion))
            return catalogoEppRepository.findSinRotacion(desde, pageable).map(this::toResponseDTO);
        return catalogoEppRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public CatalogoEppResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + id)));
    }

    @Transactional
    public void eliminar(Integer id) {
        CatalogoEpp epp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + id));
        epp.setActivo(false);
        catalogoEppRepository.save(epp);
    }
    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> listarActivos() {
        return catalogoEppRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> buscarPorNombre(String nombre) {
        return catalogoEppRepository.buscarPorNombreActivo(nombre).stream()
                .map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> buscarPorFabricante(String fabricante) {
        return catalogoEppRepository.findByFabricante(fabricante).stream()
                .map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> listarPorTipo(CatalogoEpp.TipoUso tipoUso) {
        return catalogoEppRepository.findByTipoUso(tipoUso).stream()
                .map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoEppResponseDTO> buscarPorNorma(String texto) {
        return catalogoEppRepository.findAll().stream()
                .filter(epp -> epp.getNormасAplicables().stream()
                        .anyMatch(n -> n.getCodigo().toLowerCase().contains(texto.toLowerCase())
                                || n.getNombreCorto().toLowerCase().contains(texto.toLowerCase())))
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Set<CatalogoTalla> resolverTallas(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(tallaRepository.findAllById(ids));
    }

    private Set<NormaEpp> resolverNormas(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        List<NormaEpp> encontradas = normaEppRepository.findAllById(ids);
        if (encontradas.size() != ids.size()) {
            Set<Integer> encontradosIds = encontradas.stream()
                    .map(NormaEpp::getNormaId).collect(Collectors.toSet());
            Set<Integer> faltantes = new HashSet<>(ids);
            faltantes.removeAll(encontradosIds);
            throw new BusinessException("Normas no encontradas con IDs: " + faltantes);
        }
        return new HashSet<>(encontradas);
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    public CatalogoEppResponseDTO toResponseDTO(CatalogoEpp epp) {
        Set<CatalogoTallaResponseDTO> tallasDTO = epp.getTallasDisponibles().stream()
                .map(t -> CatalogoTallaResponseDTO.builder()
                        .tallaId(t.getTallaId()).nombre(t.getNombre())
                        .descripcion(t.getDescripcion())
                        .ordenVisualizacion(t.getOrdenVisualizacion()).build())
                .collect(Collectors.toSet());

        Set<NormaEppResponseDTO> normasDTO = epp.getNormасAplicables().stream()
                .map(normaEppService::toDTO)
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
                .fichaTecnicaPath(epp.getFichaTecnicaPath())
                .color(epp.getColor())
                .tallasDisponibles(tallasDTO)
                .normasAplicables(normasDTO)
                .cantidadMinima(epp.getCantidadMinima())
                .cantidadMaxima(epp.getCantidadMaxima())
                .activo(epp.getActivo())
                .fechaCreacion(epp.getFechaCreacion())
                .fechaActualizacion(epp.getFechaActualizacion())
                .build();
    }
}
