package pe.edu.upeu.epp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.request.InventarioAreaRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioAreaUpdateDTO;
import pe.edu.upeu.epp.dto.request.TransferenciaStockDTO;
import pe.edu.upeu.epp.dto.response.InventarioAreaResponseDTO;
import pe.edu.upeu.epp.service.InventarioAreaService;

import java.util.List;

/**
 * Controller REST para gestión de Inventario por Área.
 */
@RestController
@RequestMapping("/api/v1/inventario-area")
@RequiredArgsConstructor
@Tag(name = "Inventario por Área", description = "Gestión del inventario distribuido por áreas")
@SecurityRequirement(name = "Bearer Authentication")
public class InventarioAreaController {

    private final InventarioAreaService inventarioAreaService;

    /**
     * Crear registro de inventario para un área.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Crear inventario de área",
            description = "Crea un nuevo registro de inventario para un área específica")
    public ResponseEntity<InventarioAreaResponseDTO> crear(
            @Valid @RequestBody InventarioAreaRequestDTO request) {
        InventarioAreaResponseDTO response = inventarioAreaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener inventario por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener inventario por ID",
            description = "Retorna los detalles de un registro de inventario de área")
    public ResponseEntity<InventarioAreaResponseDTO> obtenerPorId(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        InventarioAreaResponseDTO response = inventarioAreaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar inventario de un área.
     */
    @GetMapping("/area/{areaId}")
    @Operation(summary = "Listar inventario de área",
            description = "Retorna todo el inventario de un área específica")
    public ResponseEntity<List<InventarioAreaResponseDTO>> listarPorArea(
            @Parameter(description = "ID del área") @PathVariable Integer areaId) {
        List<InventarioAreaResponseDTO> response = inventarioAreaService.listarPorArea(areaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar stock bajo de un área.
     */
    @GetMapping("/area/{areaId}/stock-bajo")
    @Operation(summary = "Alertas de stock bajo por área",
            description = "Retorna EPPs con stock bajo en un área específica")
    public ResponseEntity<List<InventarioAreaResponseDTO>> listarStockBajoPorArea(
            @Parameter(description = "ID del área") @PathVariable Integer areaId) {
        List<InventarioAreaResponseDTO> response = inventarioAreaService.listarStockBajoPorArea(areaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todo el stock crítico.
     */
    @GetMapping("/alertas/stock-critico")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Alertas globales de stock crítico",
            description = "Retorna todo el stock crítico de todas las áreas")
    public ResponseEntity<List<InventarioAreaResponseDTO>> listarStockCritico() {
        List<InventarioAreaResponseDTO> response = inventarioAreaService.listarStockBajoGlobal();
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar inventario de área.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'JEFE_AREA')")
    @Operation(summary = "Actualizar inventario de área",
            description = "Actualiza la información de un inventario de área")
    public ResponseEntity<InventarioAreaResponseDTO> actualizar(
            @Parameter(description = "ID del inventario") @PathVariable Integer id,
            @Valid @RequestBody InventarioAreaUpdateDTO request) {
        InventarioAreaResponseDTO response = inventarioAreaService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Transferir stock desde inventario central a un área.
     */
    @PostMapping("/transferir")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Transferir stock central a área",
            description = "Realiza una transferencia atómica de stock desde el inventario central a un área")
    public ResponseEntity<InventarioAreaResponseDTO> transferirStock(
            @Valid @RequestBody TransferenciaStockDTO request) {
        InventarioAreaResponseDTO response = inventarioAreaService.transferirStockCentralAArea(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar inventario de área.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar inventario de área",
            description = "Elimina un registro de inventario de área (solo si cantidad es 0)")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        inventarioAreaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}