package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.CatalogoTallaRequestDTO;
import pe.edu.upeu.epp.dto.response.CatalogoTallaResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoTalla;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.CatalogoTallaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoTallaService {

    private final CatalogoTallaRepository tallaRepository;

    @Transactional
    public CatalogoTallaResponseDTO crear(CatalogoTallaRequestDTO request) {
        if (tallaRepository.existsByNombre(request.getNombre().toUpperCase())) {
            throw new BusinessException("Ya existe una talla con el nombre: " + request.getNombre());
        }
        CatalogoTalla talla = CatalogoTalla.builder()
                .nombre(request.getNombre().toUpperCase())
                .descripcion(request.getDescripcion())
                .ordenVisualizacion(request.getOrdenVisualizacion())
                .build();
        return mapToDTO(tallaRepository.save(talla));
    }

    @Transactional(readOnly = true)
    public List<CatalogoTallaResponseDTO> listarTodas() {
        return tallaRepository.findAllByOrderByOrdenVisualizacionAscNombreAsc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CatalogoTallaResponseDTO obtenerPorId(Integer id) {
        return mapToDTO(tallaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Talla no encontrada con ID: " + id)));
    }

    @Transactional
    public CatalogoTallaResponseDTO actualizar(Integer id, CatalogoTallaRequestDTO request) {
        CatalogoTalla talla = tallaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Talla no encontrada con ID: " + id));

        String nuevoNombre = request.getNombre().toUpperCase();
        if (tallaRepository.existsByNombreAndTallaIdNot(nuevoNombre, id)) {
            throw new BusinessException("Ya existe una talla con el nombre: " + request.getNombre());
        }

        talla.setNombre(nuevoNombre);
        talla.setDescripcion(request.getDescripcion());
        talla.setOrdenVisualizacion(request.getOrdenVisualizacion());

        log.info("Talla actualizada ID: {} → nombre: {}", id, nuevoNombre);
        return mapToDTO(tallaRepository.save(talla));
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!tallaRepository.existsById(id)) {
            throw new EntityNotFoundException("Talla no encontrada con ID: " + id);
        }
        tallaRepository.deleteById(id);
    }

    private CatalogoTallaResponseDTO mapToDTO(CatalogoTalla t) {
        return CatalogoTallaResponseDTO.builder()
                .tallaId(t.getTallaId())
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .ordenVisualizacion(t.getOrdenVisualizacion())
                .build();
    }
}
