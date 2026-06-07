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
import pe.edu.upeu.epp.dto.response.InventarioAgrupadoResponseDTO;
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
    @Operation(summary = "Registrar stock manual")
    public ResponseEntity<InventarioCentralResponseDTO> crear(@Valid @RequestBody InventarioCentralRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioCentralService.crear(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener por ID")
    public ResponseEntity<InventarioCentralResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioCentralService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar inventario",
               description = "Filtros: sort=alpha | filter=low_stock (combinables)")
    public ResponseEntity<Page<InventarioCentralResponseDTO>> listar(
            @PageableDefault(size = 20, sort = "ultimaActualizacion", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter) {
        return ResponseEntity.ok(inventarioCentralService.listarTodos(pageable, sort, filter));
    }

    /**
     * Vista agrupada: agrupa registros con mismo (epp, proveedor, lote)
     * mostrando todas las tallas con sus cantidades y precios en un solo objeto.
     */
    @GetMapping("/agrupado")
    @Operation(
            summary = "Inventario agrupado por EPP + lote + proveedor",
            description = "Consolida en un solo objeto todos los registros de un mismo " +
                    "EPP comprado en el mismo lote y proveedor, listando cada talla " +
                    "con su cantidad y costo unitario.")
    public ResponseEntity<List<InventarioAgrupadoResponseDTO>> listarAgrupado() {
        return ResponseEntity.ok(inventarioCentralService.listarAgrupado());
    }

    @GetMapping("/epp/{eppId}")
    @Operation(summary = "Listar por EPP")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarPorEpp(@PathVariable Integer eppId) {
        return ResponseEntity.ok(inventarioCentralService.listarPorEpp(eppId));
    }

    @GetMapping("/alertas/stock-bajo")
    @Operation(summary = "Alertas de stock bajo")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarStockBajo() {
        return ResponseEntity.ok(inventarioCentralService.listarStockBajo());
    }

    @GetMapping("/alertas/proximos-vencer")
    @Operation(summary = "Alertas de vencimiento (30 días)")
    public ResponseEntity<List<InventarioCentralResponseDTO>> listarProximosAVencer() {
        return ResponseEntity.ok(inventarioCentralService.listarProximosAVencer());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Actualizar inventario")
    public ResponseEntity<InventarioCentralResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody InventarioCentralUpdateDTO request) {
        return ResponseEntity.ok(inventarioCentralService.actualizar(id, request));
    }

    @PatchMapping("/{id}/ajustar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Ajuste manual de stock (INGRESO/SALIDA)")
    public ResponseEntity<InventarioCentralResponseDTO> ajustarStock(
            @PathVariable Integer id, @Valid @RequestBody AjusteInventarioDTO request) {
        return ResponseEntity.ok(inventarioCentralService.ajustarStock(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar (solo si cantidad = 0)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        inventarioCentralService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
