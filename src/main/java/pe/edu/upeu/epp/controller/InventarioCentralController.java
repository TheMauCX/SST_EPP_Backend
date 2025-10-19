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
import pe.edu.upeu.epp.dto.request.AjusteInventarioDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralUpdateDTO;
import pe.edu.upeu.epp.dto.response.InventarioCentralResponseDTO;
import pe.edu.upeu.epp.service.InventarioCentralService;

import java.util.List;

/**
 * Controller REST para gestión de Inventario Central.
 */
@RestController
@RequestMapping("/api/v1/inventario-central")
@RequiredArgsConstructor
@Tag(name = "Inventario Central", description = "Gestión del inventario central de EPPs")
@SecurityRequirement(name = "Bearer Authentication")
public class InventarioCentralController {

    private final InventarioCentralService inventarioCentralService;

    /**
     * Registrar nuevo stock en inventario central.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Registrar nuevo stock",
            description = "Registra un nuevo lote de EPP en el inventario central")
    public ResponseEntity<InventarioCentralResponseDTO> crear(
            @Valid @RequestBody InventarioCentralRequestDTO request) {
        InventarioCentralResponseDTO response = inventarioCentralService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener inventario por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Obtener inventario por ID",
            description = "Retorna los detalles de un registro de inventario específico")
    public ResponseEntity<InventarioCentralResponseDTO> obtenerPorId(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        InventarioCentralResponseDTO response = inventarioCentralService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todo el inventario central.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Listar todo el inventario",
            description = "Retorna una lista paginada de todo el inventario central")
    public ResponseEntity<Page<InventarioCentralResponseDTO>> listarTodo(
            @PageableDefault(size = 20, sort = "ultimaActualizacion", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<InventarioCentralResponseDTO> response = inventarioCentralService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar inventario por EPP.
     */
    @GetMapping("/epp/{eppId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Listar inventario por EPP",
            description = "Retorna todos los lotes de un EPP específico")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarPorEpp(
            @Parameter(description = "ID del EPP") @PathVariable Integer eppId) {
        List<InventarioCentralResponseDTO> response = inventarioCentralService.listarPorEpp(eppId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar stock bajo (alertas).
     */
    @GetMapping("/alertas/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Alertas de stock bajo",
            description = "Retorna EPPs con cantidad actual menor o igual a la cantidad mínima")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarStockBajo() {
        List<InventarioCentralResponseDTO> response = inventarioCentralService.listarStockBajo();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar próximos a vencer.
     */
    @GetMapping("/alertas/proximos-vencer")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Alertas de vencimiento",
            description = "Retorna EPPs que vencerán en los próximos 30 días")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarProximosAVencer() {
        List<InventarioCentralResponseDTO> response = inventarioCentralService.listarProximosAVencer();
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar información del inventario.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Actualizar inventario",
            description = "Actualiza la información de un registro de inventario")
    public ResponseEntity<InventarioCentralResponseDTO> actualizar(
            @Parameter(description = "ID del inventario") @PathVariable Integer id,
            @Valid @RequestBody InventarioCentralUpdateDTO request) {
        InventarioCentralResponseDTO response = inventarioCentralService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Ajustar stock manualmente (ingreso o salida).
     */
    @PatchMapping("/{id}/ajustar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Ajustar stock",
            description = "Realiza un ajuste manual de stock (positivo para ingreso, negativo para salida)")
    public ResponseEntity<InventarioCentralResponseDTO> ajustarStock(
            @Parameter(description = "ID del inventario") @PathVariable Integer id,
            @Valid @RequestBody AjusteInventarioDTO request) {
        InventarioCentralResponseDTO response = inventarioCentralService.ajustarStock(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar registro de inventario.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar inventario",
            description = "Elimina un registro de inventario (solo si cantidad es 0)")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        inventarioCentralService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}