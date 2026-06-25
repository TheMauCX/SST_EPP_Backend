package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.response.EstadoEppResponseDTO;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.repository.EstadoEppRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Estados EPP.
 * Maneja la lógica de negocio para consultas de estados.
 *
 * @author Sistema EPP
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EstadoEppService {

    private final EstadoEppRepository estadoEppRepository;

    /**
     * Lista todos los estados disponibles.
     *
     * @return Lista de todos los estados EPP
     */
    @Transactional(readOnly = true)
    public List<EstadoEppResponseDTO> listarTodos() {
        log.info("Listando todos los estados EPP");

        List<EstadoEpp> estados = estadoEppRepository.findAll();

        log.debug("Se encontraron {} estados", estados.size());

        return estados.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo los estados que permiten uso.
     * Estados con permiteUso = true pueden ser usados en inventarios y entregas.
     *
     * @return Lista de estados que permiten uso
     */
    @Transactional(readOnly = true)
    public List<EstadoEppResponseDTO> listarPermiteUso() {
        log.info("Listando estados que permiten uso");

        List<EstadoEpp> estados = estadoEppRepository.findByPermiteUsoTrue();

        log.debug("Se encontraron {} estados que permiten uso", estados.size());

        return estados.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un estado por su ID.
     *
     * @param id ID del estado
     * @return Estado EPP encontrado
     * @throws EntityNotFoundException si no existe el estado
     */
    @Transactional(readOnly = true)
    public EstadoEppResponseDTO obtenerPorId(Integer id) {
        log.info("Obteniendo estado EPP con ID: {}", id);

        EstadoEpp estado = estadoEppRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Estado no encontrado con ID: {}", id);
                    return new EntityNotFoundException("Estado EPP no encontrado con ID: " + id);
                });

        log.debug("Estado encontrado: {}", estado.getNombre());

        return mapToResponseDTO(estado);
    }

    /**
     * Obtiene un estado por su nombre.
     * Método útil para validaciones internas.
     *
     * @param nombre Nombre del estado
     * @return Estado EPP encontrado
     * @throws EntityNotFoundException si no existe el estado
     */
    @Transactional(readOnly = true)
    public EstadoEppResponseDTO obtenerPorNombre(String nombre) {
        log.info("Obteniendo estado EPP con nombre: {}", nombre);

        EstadoEpp estado = estadoEppRepository.findByNombre(nombre)
                .orElseThrow(() -> {
                    log.error("Estado no encontrado con nombre: {}", nombre);
                    return new EntityNotFoundException("Estado EPP no encontrado: " + nombre);
                });

        return mapToResponseDTO(estado);
    }

    /**
     * Verifica si existe un estado con el ID dado.
     *
     * @param id ID del estado
     * @return true si existe, false si no
     */
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        boolean existe = estadoEppRepository.existsById(id);
        log.debug("Estado con ID {} existe: {}", id, existe);
        return existe;
    }

    /**
     * Verifica si un estado permite uso.
     *
     * @param id ID del estado
     * @return true si permite uso, false si no
     * @throws EntityNotFoundException si no existe el estado
     */
    @Transactional(readOnly = true)
    public boolean permiteUso(Integer id) {
        EstadoEpp estado = estadoEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + id));

        boolean permite = Boolean.TRUE.equals(estado.getPermiteUso());
        log.debug("Estado {} permite uso: {}", estado.getNombre(), permite);

        return permite;
    }

    // ============================================
    // MÉTODOS PRIVADOS DE MAPEO
    // ============================================

    /**
     * Mapea una entidad EstadoEpp a DTO de respuesta.
     *
     * @param estado Entidad EstadoEpp
     * @return DTO de respuesta
     */
    private EstadoEppResponseDTO mapToResponseDTO(EstadoEpp estado) {
        return EstadoEppResponseDTO.builder()
                .estadoId(estado.getEstadoId())
                .nombre(estado.getNombre())
                .descripcion(estado.getDescripcion())
                .permiteUso(estado.getPermiteUso())
                .colorHex(estado.getColorHex())
                .build();
    }
}