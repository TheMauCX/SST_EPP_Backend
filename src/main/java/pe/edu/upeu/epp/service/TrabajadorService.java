package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.TrabajadorRequestDTO;
import pe.edu.upeu.epp.dto.request.TrabajadorUpdateDTO;
import pe.edu.upeu.epp.dto.response.TrabajadorQrResponseDTO;
import pe.edu.upeu.epp.dto.response.TrabajadorResponseDTO;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.Trabajador;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.AreaRepository;
import pe.edu.upeu.epp.repository.TrabajadorRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para Trabajadores.
 * Incluye generación automática de código QR.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final AreaRepository areaRepository;

    /**
     * Crear un nuevo trabajador con código QR automático.
     */
    @Transactional
    public TrabajadorQrResponseDTO crear(TrabajadorRequestDTO request) {
        log.info("Creando nuevo trabajador: {} {}", request.getNombres(), request.getApellidos());

        // Validar que el DNI sea único
        if (trabajadorRepository.findByDni(request.getDni()).isPresent()) {
            throw new BusinessException("Ya existe un trabajador con el DNI: " + request.getDni());
        }

        // Validar que el área exista
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + request.getAreaId()));

        // Generar código QR automáticamente
        String codigoQr = generarCodigoQr();

        // Crear entidad
        Trabajador trabajador = Trabajador.builder()
                .dni(request.getDni())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .codigoQrPhotocheck(codigoQr)
                .area(area)
                .puesto(request.getPuesto())
                .fechaIngreso(request.getFechaIngreso() != null ? request.getFechaIngreso() : LocalDate.now())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .estado(Trabajador.EstadoTrabajador.ACTIVO)
                .build();

        trabajador = trabajadorRepository.save(trabajador);

        log.info("Trabajador creado exitosamente con ID: {} y código QR: {}",
                trabajador.getTrabajadorId(), trabajador.getCodigoQrPhotocheck());

        return mapToQrResponseDTO(trabajador);
    }

    /**
     * Obtener un trabajador por ID.
     */
    @Transactional(readOnly = true)
    public TrabajadorResponseDTO obtenerPorId(Integer id) {
        log.debug("Buscando trabajador con ID: {}", id);

        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con ID: " + id));

        return mapToResponseDTO(trabajador);
    }

    /**
     * Buscar trabajador por DNI.
     */
    @Transactional(readOnly = true)
    public TrabajadorResponseDTO buscarPorDni(String dni) {
        log.debug("Buscando trabajador con DNI: {}", dni);

        Trabajador trabajador = trabajadorRepository.findByDni(dni)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con DNI: " + dni));

        return mapToResponseDTO(trabajador);
    }

    /**
     * Buscar trabajador por código QR.
     */
    @Transactional(readOnly = true)
    public TrabajadorQrResponseDTO buscarPorCodigoQr(String codigoQr) {
        log.debug("Buscando trabajador con código QR: {}", codigoQr);

        Trabajador trabajador = trabajadorRepository.findByCodigoQrPhotocheck(codigoQr)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con código QR: " + codigoQr));

        return mapToQrResponseDTO(trabajador);
    }

    /**
     * Listar todos los trabajadores con paginación.
     */
    @Transactional(readOnly = true)
    public Page<TrabajadorResponseDTO> listarTodos(Pageable pageable) {
        log.debug("Listando todos los trabajadores - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return trabajadorRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Listar trabajadores por área.
     */
    @Transactional(readOnly = true)
    public List<TrabajadorResponseDTO> listarPorArea(Integer areaId) {
        log.debug("Listando trabajadores del área: {}", areaId);

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + areaId));

        return trabajadorRepository.findByArea(area)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar trabajadores activos de un área.
     */
    @Transactional(readOnly = true)
    public List<TrabajadorResponseDTO> listarActivosPorArea(Integer areaId) {
        log.debug("Listando trabajadores activos del área: {}", areaId);

        return trabajadorRepository.findTrabajadoresActivosPorArea(areaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar trabajadores por nombre.
     */
    @Transactional(readOnly = true)
    public List<TrabajadorResponseDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando trabajadores con nombre que contiene: {}", nombre);

        return trabajadorRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un trabajador existente.
     */
    @Transactional
    public TrabajadorResponseDTO actualizar(Integer id, TrabajadorUpdateDTO request) {
        log.info("Actualizando trabajador con ID: {}", id);

        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con ID: " + id));

        // Actualizar solo los campos proporcionados
        if (request.getNombres() != null) {
            trabajador.setNombres(request.getNombres());
        }

        if (request.getApellidos() != null) {
            trabajador.setApellidos(request.getApellidos());
        }

        if (request.getAreaId() != null) {
            Area area = areaRepository.findById(request.getAreaId())
                    .orElseThrow(() -> new EntityNotFoundException("Área no encontrada"));
            trabajador.setArea(area);
        }

        if (request.getPuesto() != null) {
            trabajador.setPuesto(request.getPuesto());
        }

        if (request.getFechaIngreso() != null) {
            trabajador.setFechaIngreso(request.getFechaIngreso());
        }

        if (request.getTelefono() != null) {
            trabajador.setTelefono(request.getTelefono());
        }

        if (request.getEmail() != null) {
            trabajador.setEmail(request.getEmail());
        }

        if (request.getEstado() != null) {
            trabajador.setEstado(request.getEstado());
        }

        trabajador = trabajadorRepository.save(trabajador);

        log.info("Trabajador actualizado exitosamente: {}", id);
        return mapToResponseDTO(trabajador);
    }

    /**
     * Cambiar estado de un trabajador (activar/inactivar/suspender).
     */
    @Transactional
    public TrabajadorResponseDTO cambiarEstado(Integer id, Trabajador.EstadoTrabajador nuevoEstado) {
        log.info("Cambiando estado del trabajador {} a {}", id, nuevoEstado);

        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con ID: " + id));

        trabajador.setEstado(nuevoEstado);
        trabajador = trabajadorRepository.save(trabajador);

        log.info("Estado del trabajador actualizado exitosamente");
        return mapToResponseDTO(trabajador);
    }

    /**
     * Regenerar código QR para un trabajador.
     */
    @Transactional
    public TrabajadorQrResponseDTO regenerarCodigoQr(Integer id) {
        log.info("Regenerando código QR para trabajador: {}", id);

        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con ID: " + id));

        String nuevoCodigoQr = generarCodigoQr();
        trabajador.setCodigoQrPhotocheck(nuevoCodigoQr);

        trabajador = trabajadorRepository.save(trabajador);

        log.info("Código QR regenerado exitosamente: {}", nuevoCodigoQr);
        return mapToQrResponseDTO(trabajador);
    }

    /**
     * Genera un código QR único.
     * Formato: QR-YYYYMMDD-NNNNNN
     */
    private String generarCodigoQr() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Obtener el último trabajador para generar el correlativo
        long count = trabajadorRepository.count();
        String correlativo = String.format("%06d", count + 1);

        String codigoQr = "QR-" + fecha + "-" + correlativo;

        // Verificar que sea único (por si acaso)
        while (trabajadorRepository.findByCodigoQrPhotocheck(codigoQr).isPresent()) {
            count++;
            correlativo = String.format("%06d", count + 1);
            codigoQr = "QR-" + fecha + "-" + correlativo;
        }

        return codigoQr;
    }

    /**
     * Mapea una entidad Trabajador a su DTO de respuesta.
     */
    private TrabajadorResponseDTO mapToResponseDTO(Trabajador trabajador) {
        return TrabajadorResponseDTO.builder()
                .trabajadorId(trabajador.getTrabajadorId())
                .dni(trabajador.getDni())
                .nombres(trabajador.getNombres())
                .apellidos(trabajador.getApellidos())
                .nombreCompleto(trabajador.getNombres() + " " + trabajador.getApellidos())
                .codigoQrPhotocheck(trabajador.getCodigoQrPhotocheck())
                .areaId(trabajador.getArea().getAreaId())
                .areaNombre(trabajador.getArea().getNombreArea())
                .puesto(trabajador.getPuesto())
                .fechaIngreso(trabajador.getFechaIngreso())
                .telefono(trabajador.getTelefono())
                .email(trabajador.getEmail())
                .estado(trabajador.getEstado())
                .fechaCreacion(trabajador.getFechaCreacion())
                .fechaActualizacion(trabajador.getFechaActualizacion())
                .build();
    }

    /**
     * Mapea una entidad Trabajador a su DTO de respuesta con QR.
     */
    private TrabajadorQrResponseDTO mapToQrResponseDTO(Trabajador trabajador) {
        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" +
                trabajador.getCodigoQrPhotocheck();

        return TrabajadorQrResponseDTO.builder()
                .trabajadorId(trabajador.getTrabajadorId())
                .nombreCompleto(trabajador.getNombres() + " " + trabajador.getApellidos())
                .dni(trabajador.getDni())
                .codigoQrPhotocheck(trabajador.getCodigoQrPhotocheck())
                .areaNombre(trabajador.getArea().getNombreArea())
                .puesto(trabajador.getPuesto())
                .qrImageUrl(qrImageUrl)
                .build();
    }
}