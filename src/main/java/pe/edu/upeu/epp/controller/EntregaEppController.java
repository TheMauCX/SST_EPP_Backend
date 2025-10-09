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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.request.EntregaEppRequestDTO;
import pe.edu.upeu.epp.dto.response.EntregaDetalleResponseDTO;
import pe.edu.upeu.epp.dto.response.EntregaEppResponseDTO;
import pe.edu.upeu.epp.service.EntregaEppService;

import java.time.LocalDate;

/**
 * Controller REST para gestión de Entregas de EPP.
 * CRÍTICO: Endpoint más importante del sistema.
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
    @PreAuthorize("hasAnyRole('JEFE_AREA', 'SUPERVISOR_SST')")
    @Operation(
            summary = "Registrar entrega de EPP",
            description = "Registra la entrega de uno o más EPPs a un trabajador. " +
                    "Actualiza automáticamente el inventario del área. " +
                    "Para EPPs consumibles, resta la cantidad del inventario. " +
                    "Para EPPs durables, actualiza el estado de la instancia."
    )
    public ResponseEntity<EntregaEppResponseDTO> registrarEntrega(
            @Valid @RequestBody EntregaEppRequestDTO request) {
        EntregaEppResponseDTO response = entregaEppService.registrarEntrega(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener detalle completo de una entrega.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de entrega",
            description = "Retorna todos los detalles de una entrega específica")
    public ResponseEntity<EntregaDetalleResponseDTO> obtenerDetalle(
            @Parameter(description = "ID de la entrega") @PathVariable Integer id) {
        EntregaDetalleResponseDTO response = entregaEppService.obtenerDetalle(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todas las entregas con paginación.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('JEFE_AREA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Listar todas las entregas",
            description = "Retorna una lista paginada de todas las entregas")
    public ResponseEntity<Page<EntregaEppResponseDTO>> listarTodas(
            @PageableDefault(size = 20, sort = "fechaEntrega", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<EntregaEppResponseDTO> response = entregaEppService.listarTodas(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar entregas por trabajador.
     */
    @GetMapping("/trabajador/{trabajadorId}")
    @Operation(summary = "Listar entregas por trabajador",
            description = "Retorna el historial de entregas de un trabajador específico")
    public ResponseEntity<Page<EntregaEppResponseDTO>> listarPorTrabajador(
            @Parameter(description = "ID del trabajador") @PathVariable Integer trabajadorId,
            @PageableDefault(size = 20, sort = "fechaEntrega", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<EntregaEppResponseDTO> response = entregaEppService.listarPorTrabajador(trabajadorId, pageable);
        return ResponseEntity.ok(response);
    }
}