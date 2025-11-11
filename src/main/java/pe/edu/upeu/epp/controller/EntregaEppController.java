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
import pe.edu.upeu.epp.dto.request.EntregaEppRequestDTO;
import pe.edu.upeu.epp.dto.response.EntregaDetalleResponseDTO;
import pe.edu.upeu.epp.dto.response.EntregaEppResponseDTO;
import pe.edu.upeu.epp.service.EntregaEppService;

import java.security.Principal; // <-- IMPORTANTE

/**
 * Controller REST para gestión de Entregas de EPP.
 * ACTUALIZADO: Sincronizado con EntregaEppService, implementa paginación
 * y pasa el 'username' del usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/entregas")
@RequiredArgsConstructor
@Tag(name = "Entregas EPP", description = "Gestión de entregas de EPP a trabajadores")
@SecurityRequirement(name = "Bearer Authentication")
public class EntregaEppController {

    private final EntregaEppService entregaEppService;

    /**
     * Registrar una nueva entrega de EPP.
     * CRÍTICO: Transacción atómica que actualiza inventarios.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA','JEFE_AREA', 'SUPERVISOR_SST')")
    @Operation(
            summary = "Registrar entrega de EPP",
            description = "Registra la entrega de uno o más EPPs a un trabajador. " +
                    "Requiere el 'username' del Jefe de Área (obtenido del token) para auditoría."
    )
    public ResponseEntity<EntregaEppResponseDTO> registrarEntrega(
            @Valid @RequestBody EntregaEppRequestDTO request,
            Principal principal) { // <-- CAMBIO: Se inyecta el usuario autenticado

        // Obtenemos el username del token JWT
        String username = principal.getName();

        EntregaEppResponseDTO response = entregaEppService.registrarEntregaEpp(request, username); // <-- CAMBIO: Se pasa el username
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener detalle completo de una entrega.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de entrega",
            description = "Retorna todos los detalles de una entrega específica (consumibles y duraderos)")
    public ResponseEntity<EntregaDetalleResponseDTO> obtenerDetalle(
            @Parameter(description = "ID de la entrega") @PathVariable Integer id) {
        // CAMBIO: El método en el servicio se llama findEntregaById
        EntregaDetalleResponseDTO response = entregaEppService.findEntregaById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todas las entregas con paginación.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA','JEFE_AREA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Listar todas las entregas (Paginado)",
            description = "Retorna una lista paginada de todas las entregas")
    public ResponseEntity<Page<EntregaEppResponseDTO>> listarTodas(
            @PageableDefault(size = 20, sort = "fechaEntrega", direction = Sort.Direction.DESC)
            Pageable pageable) {
        // CAMBIO: El método en el servicio se llama findAllEntregas
        Page<EntregaEppResponseDTO> response = entregaEppService.findAllEntregas(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar entregas por ID de trabajador (Paginado).
     */
    @GetMapping("/trabajador/id/{trabajadorId}")
    @Operation(summary = "Listar entregas por ID de trabajador (Paginado)",
            description = "Retorna el historial paginado de entregas de un trabajador específico por su ID")
    public ResponseEntity<Page<EntregaEppResponseDTO>> listarPorTrabajadorId(
            @Parameter(description = "ID del trabajador") @PathVariable Integer trabajadorId,
            @PageableDefault(size = 20, sort = "fechaEntrega", direction = Sort.Direction.DESC)
            Pageable pageable) {
        // CAMBIO: El método en el servicio se llama findEntregasByTrabajadorId
        Page<EntregaEppResponseDTO> response = entregaEppService.findEntregasByTrabajadorId(trabajadorId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar entregas por DNI de trabajador (Paginado).
     */
    @GetMapping("/trabajador/dni/{dni}")
    @Operation(summary = "Listar entregas por DNI de trabajador (Paginado)",
            description = "Retorna el historial paginado de entregas de un trabajador específico por su DNI")
    public ResponseEntity<Page<EntregaEppResponseDTO>> listarPorTrabajadorDni(
            @Parameter(description = "DNI del trabajador") @PathVariable String dni,
            @PageableDefault(size = 20, sort = "fechaEntrega", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<EntregaEppResponseDTO> response = entregaEppService.findEntregasByTrabajadorDni(dni, pageable);
        return ResponseEntity.ok(response);
    }
}