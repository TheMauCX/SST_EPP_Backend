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

@RestController
@RequestMapping("/api/v1/inventario-central")
@RequiredArgsConstructor
@Tag(name = "Inventario Central", description = "Gestión del inventario central de EPPs")
@SecurityRequirement(name = "Bearer Authentication")
public class InventarioCentralController {

    private final InventarioCentralService inventarioCentralService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Registrar nuevo stock")
    public ResponseEntity<InventarioCentralResponseDTO> crear(
            @Valid @RequestBody InventarioCentralRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioCentralService.crear(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Obtener inventario por ID")
    public ResponseEntity<InventarioCentralResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioCentralService.obtenerPorId(id));
    }

    /**
     * HU-24: Listar inventario central con filtros de orden y bajo stock.
     *
     * Parámetros:
     *   sort=alpha       → ordena alfabéticamente por nombre de EPP
     *   filter=low_stock → muestra solo EPPs con stock ≤ mínimo
     *
     * Se pueden combinar: GET /inventario-central?sort=alpha&filter=low_stock
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(
            summary = "Listar inventario central",
            description = "Lista paginada. " +
                    "Filtros: sort=alpha (orden alfabético) | filter=low_stock (solo bajo stock). " +
                    "Combinables simultáneamente.")
    public ResponseEntity<Page<InventarioCentralResponseDTO>> listarTodo(
            @PageableDefault(size = 20, sort = "ultimaActualizacion", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "Ordenar alfabéticamente: alpha")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Filtrar por bajo stock: low_stock")
            @RequestParam(required = false) String filter) {
        return ResponseEntity.ok(inventarioCentralService.listarTodos(pageable, sort, filter));
    }

    @GetMapping("/epp/{eppId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Listar inventario por EPP")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarPorEpp(@PathVariable Integer eppId) {
        return ResponseEntity.ok(inventarioCentralService.listarPorEpp(eppId));
    }

    @GetMapping("/alertas/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Alertas de stock bajo")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarStockBajo() {
        return ResponseEntity.ok(inventarioCentralService.listarStockBajo());
    }

    @GetMapping("/alertas/proximos-vencer")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST', 'COORDINADOR_SST')")
    @Operation(summary = "Alertas de vencimiento")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarProximosAVencer() {
        return ResponseEntity.ok(inventarioCentralService.listarProximosAVencer());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Actualizar inventario")
    public ResponseEntity<InventarioCentralResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody InventarioCentralUpdateDTO request) {
        return ResponseEntity.ok(inventarioCentralService.actualizar(id, request));
    }

    @PatchMapping("/{id}/ajustar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Ajustar stock manualmente")
    public ResponseEntity<InventarioCentralResponseDTO> ajustarStock(
            @PathVariable Integer id,
            @Valid @RequestBody AjusteInventarioDTO request) {
        return ResponseEntity.ok(inventarioCentralService.ajustarStock(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar inventario (solo si cantidad = 0)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        inventarioCentralService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
