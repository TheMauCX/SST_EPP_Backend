package pe.edu.upeu.epp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.request.TrabajadorRequestDTO;
import pe.edu.upeu.epp.dto.request.TrabajadorUpdateDTO;
import pe.edu.upeu.epp.dto.response.TrabajadorQrResponseDTO;
import pe.edu.upeu.epp.dto.response.TrabajadorResponseDTO;
import pe.edu.upeu.epp.entity.Trabajador;
import pe.edu.upeu.epp.service.TrabajadorService;

import java.util.List;

/**
 * Controller REST para gestión de Trabajadores.
 */
@RestController
@RequestMapping("/api/v1/trabajadores")
@RequiredArgsConstructor
@Tag(name = "Trabajadores", description = "Gestión de trabajadores y códigos QR")
@SecurityRequirement(name = "Bearer Authentication")
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    /**
     * Crear un nuevo trabajador con código QR automático.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'JEFE_AREA')")
    @Operation(summary = "Crear nuevo trabajador",
            description = "Crea un nuevo trabajador y genera automáticamente su código QR para photocheck")
    public ResponseEntity<TrabajadorQrResponseDTO> crear(
            @Valid @RequestBody TrabajadorRequestDTO request) {
        TrabajadorQrResponseDTO response = trabajadorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un trabajador por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener trabajador por ID", description = "Retorna los detalles de un trabajador específico")
    public ResponseEntity<TrabajadorResponseDTO> obtenerPorId(
            @Parameter(description = "ID del trabajador") @PathVariable Integer id) {
        TrabajadorResponseDTO response = trabajadorService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar trabajador por DNI.
     */
    @GetMapping("/dni/{dni}")
    @Operation(summary = "Buscar por DNI", description = "Busca un trabajador por su número de DNI")
    public ResponseEntity<TrabajadorResponseDTO> buscarPorDni(
            @Parameter(description = "DNI del trabajador") @PathVariable String dni) {
        TrabajadorResponseDTO response = trabajadorService.buscarPorDni(dni);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar trabajador por código QR (escaneo de photocheck).
     */
    @GetMapping("/qr/{codigoQr}")
    @Operation(summary = "Buscar por código QR",
            description = "Busca un trabajador escaneando el código QR de su photocheck")
    public ResponseEntity<TrabajadorQrResponseDTO> buscarPorCodigoQr(
            @Parameter(description = "Código QR del photocheck") @PathVariable String codigoQr) {
        TrabajadorQrResponseDTO response = trabajadorService.buscarPorCodigoQr(codigoQr);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los trabajadores con paginación.
     */
    @GetMapping
    @Operation(summary = "Listar todos los trabajadores", description = "Retorna una lista paginada de todos los trabajadores")
    public ResponseEntity<Page<TrabajadorResponseDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "apellidos", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<TrabajadorResponseDTO> response = trabajadorService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar trabajadores por área.
     */
    @GetMapping("/area/{areaId}")
    @Operation(summary = "Listar por área", description = "Retorna todos los trabajadores de un área específica")
    public ResponseEntity<List<TrabajadorResponseDTO>> listarPorArea(
            @Parameter(description = "ID del área") @PathVariable Integer areaId) {
        List<TrabajadorResponseDTO> response = trabajadorService.listarPorArea(areaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar trabajadores activos de un área.
     */
    @GetMapping("/area/{areaId}/activos")
    @Operation(summary = "Listar activos por área", description = "Retorna solo los trabajadores activos de un área")
    public ResponseEntity<List<TrabajadorResponseDTO>> listarActivosPorArea(
            @Parameter(description = "ID del área") @PathVariable Integer areaId) {
        List<TrabajadorResponseDTO> response = trabajadorService.listarActivosPorArea(areaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar trabajadores por nombre.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nombre", description = "Busca trabajadores cuyo nombre o apellido contenga el texto especificado")
    public ResponseEntity<List<TrabajadorResponseDTO>> buscarPorNombre(
            @Parameter(description = "Texto a buscar") @RequestParam String nombre) {
        List<TrabajadorResponseDTO> response = trabajadorService.buscarPorNombre(nombre);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un trabajador existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'JEFE_AREA')")
    @Operation(summary = "Actualizar trabajador", description = "Actualiza la información de un trabajador existente")
    public ResponseEntity<TrabajadorResponseDTO> actualizar(
            @Parameter(description = "ID del trabajador") @PathVariable Integer id,
            @Valid @RequestBody TrabajadorUpdateDTO request) {
        TrabajadorResponseDTO response = trabajadorService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cambiar estado de un trabajador.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'JEFE_AREA')")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de un trabajador (ACTIVO, INACTIVO, SUSPENDIDO)")
    public ResponseEntity<TrabajadorResponseDTO> cambiarEstado(
            @Parameter(description = "ID del trabajador") @PathVariable Integer id,
            @Parameter(description = "Nuevo estado") @RequestParam Trabajador.EstadoTrabajador estado) {
        TrabajadorResponseDTO response = trabajadorService.cambiarEstado(id, estado);
        return ResponseEntity.ok(response);
    }

    /**
     * Regenerar código QR para un trabajador.
     */
    @PostMapping("/{id}/regenerar-qr")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Regenerar código QR",
            description = "Genera un nuevo código QR para el photocheck del trabajador")
    public ResponseEntity<TrabajadorQrResponseDTO> regenerarCodigoQr(
            @Parameter(description = "ID del trabajador") @PathVariable Integer id) {
        TrabajadorQrResponseDTO response = trabajadorService.regenerarCodigoQr(id);
        return ResponseEntity.ok(response);
    }
}