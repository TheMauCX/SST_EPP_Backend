package pe.edu.upeu.epp.service;

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
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoEppService {

    private final CatalogoEppRepository catalogoEppRepository;
    private final AzureStorageService azureStorageService;

    @Transactional
    public CatalogoEppResponseDTO crear(CatalogoEppRequestDTO request, MultipartFile fotoArchivo) {
        log.info("Creando nuevo EPP: {}", request.getNombreEpp());

        String urlFoto = null;
        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            urlFoto = azureStorageService.subirArchivo(fotoArchivo);
            log.info("Foto subida a Azure: {}", urlFoto);
        } else {
            // Por si envían la URL como texto en lugar del archivo
            urlFoto = request.getFotoReferencia();
        }

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
                .fotoReferencia(urlFoto)
                .activo(true)
                .build();

        catalogoEpp = catalogoEppRepository.save(catalogoEpp);
        return mapToResponseDTO(catalogoEpp);
    }

    @Transactional
    public CatalogoEppResponseDTO actualizar(Integer id, CatalogoEppUpdateDTO request, MultipartFile fotoArchivo) {
        log.info("Actualizando EPP con ID: {}", id);

        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));

        // Subir nueva foto si se envió una
        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            String urlFoto = azureStorageService.subirArchivo(fotoArchivo);
            catalogoEpp.setFotoReferencia(urlFoto);
            log.info("Foto actualizada en Azure: {}", urlFoto);
        } else if (request.getFotoReferencia() != null) {
            catalogoEpp.setFotoReferencia(request.getFotoReferencia());
        }

        if (request.getNombreEpp() != null) catalogoEpp.setNombreEpp(request.getNombreEpp());
        if (request.getTipoUso() != null) catalogoEpp.setTipoUso(request.getTipoUso());
        if (request.getAprobacionesNormas() != null) catalogoEpp.setAprobacionesNormas(request.getAprobacionesNormas());
        if (request.getCaracteristicas() != null) catalogoEpp.setCaracteristicas(request.getCaracteristicas());
        if (request.getFabricante() != null) catalogoEpp.setFabricante(request.getFabricante());
        if (request.getTiempoUsoFabricante() != null) catalogoEpp.setTiempoUsoFabricante(request.getTiempoUsoFabricante());
        if (request.getTiempoUsoOperacion() != null) catalogoEpp.setTiempoUsoOperacion(request.getTiempoUsoOperacion());
        if (request.getCondicionesMantenimiento() != null) catalogoEpp.setCondicionesMantenimiento(request.getCondicionesMantenimiento());
        if (request.getCondicionesAlmacenamiento() != null) catalogoEpp.setCondicionesAlmacenamiento(request.getCondicionesAlmacenamiento());
        if (request.getCondicionesCambioPrematuro() != null) catalogoEpp.setCondicionesCambioPrematuro(request.getCondicionesCambioPrematuro());
        if (request.getActivo() != null) catalogoEpp.setActivo(request.getActivo());

        catalogoEpp = catalogoEppRepository.save(catalogoEpp);
        return mapToResponseDTO(catalogoEpp);
    }

    @Transactional(readOnly = true)
    public CatalogoEppResponseDTO obtenerPorId(Integer id) {
        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));
        return mapToResponseDTO(catalogoEpp);
    }

    @Transactional(readOnly = true)
    public Page<CatalogoEppResponseDTO> listarTodos(Pageable pageable) {
        return catalogoEppRepository.findAll(pageable).map(this::mapToResponseDTO);
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
        CatalogoEpp catalogoEpp = catalogoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + id));
        catalogoEpp.setActivo(false);
        catalogoEppRepository.save(catalogoEpp);
    }

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