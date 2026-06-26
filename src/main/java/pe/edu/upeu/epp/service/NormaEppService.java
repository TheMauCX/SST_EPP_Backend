package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.NormaEppRequestDTO;
import pe.edu.upeu.epp.dto.response.NormaEppResponseDTO;
import pe.edu.upeu.epp.entity.NormaEpp;
import pe.edu.upeu.epp.entity.NormaEpp.Categoria;
import pe.edu.upeu.epp.entity.NormaEpp.Organismo;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.NormaEppRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NormaEppService {

    private final NormaEppRepository normaEppRepository;

    @Transactional
    public NormaEppResponseDTO crear(NormaEppRequestDTO request) {
        if (normaEppRepository.existsByCodigo(request.getCodigo()))
            throw new BusinessException("Ya existe una norma con el código: " + request.getCodigo());

        NormaEpp norma = NormaEpp.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombreCorto(request.getNombreCorto())
                .descripcion(request.getDescripcion())
                .organismo(request.getOrganismo())
                .categoria(request.getCategoria())
                .iconoUrl(request.getIconoUrl())
                .activo(true)
                .build();

        return toDTO(normaEppRepository.save(norma));
    }

    @Transactional
    public NormaEppResponseDTO actualizar(Integer id, NormaEppRequestDTO request) {
        NormaEpp norma = normaEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Norma no encontrada: " + id));

        if (!norma.getCodigo().equals(request.getCodigo())
                && normaEppRepository.existsByCodigo(request.getCodigo()))
            throw new BusinessException("Ya existe una norma con el código: " + request.getCodigo());

        norma.setCodigo(request.getCodigo().trim().toUpperCase());
        norma.setNombreCorto(request.getNombreCorto());
        norma.setDescripcion(request.getDescripcion());
        norma.setOrganismo(request.getOrganismo());
        norma.setCategoria(request.getCategoria());
        if (request.getIconoUrl() != null) norma.setIconoUrl(request.getIconoUrl());

        return toDTO(normaEppRepository.save(norma));
    }

    @Transactional
    public void desactivar(Integer id) {
        NormaEpp norma = normaEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Norma no encontrada: " + id));
        norma.setActivo(false);
        normaEppRepository.save(norma);
    }

    @Transactional(readOnly = true)
    public List<NormaEppResponseDTO> listarActivas() {
        return normaEppRepository.findByActivoTrue().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NormaEppResponseDTO> listarTodas() {
        return normaEppRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NormaEppResponseDTO> listarPorOrganismo(String organismoStr) {
        Organismo organismo = Organismo.valueOf(organismoStr.toUpperCase());
        return normaEppRepository.findByOrganismoAndActivoTrue(organismo).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NormaEppResponseDTO> listarPorCategoria(String categoriaStr) {
        Categoria categoria = Categoria.valueOf(categoriaStr.toUpperCase());
        return normaEppRepository.findByCategoriaAndActivoTrue(categoria).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NormaEppResponseDTO obtenerPorId(Integer id) {
        return toDTO(normaEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Norma no encontrada: " + id)));
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    public NormaEppResponseDTO toDTO(NormaEpp norma) {
        return NormaEppResponseDTO.builder()
                .normaId(norma.getNormaId())
                .codigo(norma.getCodigo())
                .nombreCorto(norma.getNombreCorto())
                .descripcion(norma.getDescripcion())
                .organismo(norma.getOrganismo())
                .categoria(norma.getCategoria())
                .iconoUrl(norma.getIconoUrl())
                .activo(norma.getActivo())
                .build();
    }
}
