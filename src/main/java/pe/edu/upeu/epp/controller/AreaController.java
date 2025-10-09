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
import pe.edu.upeu.epp.dto.request.AreaRequestDTO;
import pe.edu.upeu.epp.dto.request.AreaUpdateDTO;
import pe.edu.upeu.epp.dto.response.AreaResponseDTO;
import pe.edu.upeu.epp.service.AreaService;

import java.util.List;

/**
 * Controller REST para gestión de Áreas.
 */
@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
@Tag(name = "Áreas", description = "Gestión de áreas de trabajo")
@SecurityRequirement(name = "Bearer Authentication")
public class AreaController {

    private final AreaService areaService;

    /**
     * Crear una nueva área.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Crear nueva área", description = "Crea una nueva área de trabajo")
    public ResponseEntity<AreaResponseDTO> crear(
            @Valid @RequestBody AreaRequestDTO request) {
        AreaResponseDTO response = areaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un área por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener área por ID", description = "Retorna los detalles de un área específica")
    public ResponseEntity<AreaResponseDTO> obtenerPorId(
            @Parameter(description = "ID del área") @PathVariable Integer id) {
        AreaResponseDTO response = areaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todas las áreas con paginación.
     */
    @GetMapping
    @Operation(summary = "Listar todas las áreas", description = "Retorna una lista paginada de todas las áreas")
    public ResponseEntity<Page<AreaResponseDTO>> listarTodas(
            @PageableDefault(size = 20, sort = "nombreArea", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<AreaResponseDTO> response = areaService.listarTodas(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar solo áreas activas.
     */
    @GetMapping("/activas")
    @Operation(summary = "Listar áreas activas", description = "Retorna solo las áreas que están activas")
    public ResponseEntity<List<AreaResponseDTO>> listarActivas() {
        List<AreaResponseDTO> response = areaService.listarActivas();
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un área existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Actualizar área", description = "Actualiza la información de un área existente")
    public ResponseEntity<AreaResponseDTO> actualizar(
            @Parameter(description = "ID del área") @PathVariable Integer id,
            @Valid @RequestBody AreaUpdateDTO request) {
        AreaResponseDTO response = areaService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar (desactivar) un área.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar área", description = "Desactiva un área (no la elimina físicamente)")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del área") @PathVariable Integer id) {
        areaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}