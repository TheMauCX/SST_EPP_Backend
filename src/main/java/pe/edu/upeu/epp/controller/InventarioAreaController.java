package pe.edu.upeu.epp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.request.InventarioAreaRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioAreaUpdateDTO;
import pe.edu.upeu.epp.dto.request.TransferenciaStockDTO;
import pe.edu.upeu.epp.dto.response.InventarioAreaAgrupadoResponseDTO;
import pe.edu.upeu.epp.dto.response.InventarioAreaResponseDTO;
import pe.edu.upeu.epp.service.InventarioAreaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario-area")
@RequiredArgsConstructor
@Tag(name = "Inventario Área", description = "Gestión del inventario por área")
@SecurityRequirement(name = "Bearer Authentication")
public class InventarioAreaController {

    private final InventarioAreaService inventarioAreaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA','SUPERVISOR_SST')")
    public ResponseEntity<InventarioAreaResponseDTO> crear(@Valid @RequestBody InventarioAreaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioAreaService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioAreaResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioAreaService.obtenerPorId(id));
    }

    @GetMapping("/area/{areaId}")
    @Operation(summary = "Listar inventario plano de un área (una fila por talla)")
    public ResponseEntity<List<InventarioAreaResponseDTO>> listarPorArea(@PathVariable Integer areaId) {
        return ResponseEntity.ok(inventarioAreaService.listarPorArea(areaId));
    }

    @GetMapping("/agrupado")
    @Operation(
            summary = "Inventario de área agrupado global (paginado)",
            description = "Consolida todas las tallas de un mismo EPP en un área en un solo objeto. " +
                    "Incluye todos los inventarios de todas las áreas. " +
                    "Soporta paginación: ?page=0&size=10")
    public ResponseEntity<Page<InventarioAreaAgrupadoResponseDTO>> listarAgrupadoGlobal(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(inventarioAreaService.listarAgrupadoGlobal(pageable));
    }

    @GetMapping("/agrupado/area/{areaId}")
    @Operation(
            summary = "Inventario agrupado de un área específica (paginado)",
            description = "Muestra todas las tallas de cada EPP en el área seleccionada. " +
                    "Ideal para la pantalla de detalle de un área.")
    public ResponseEntity<Page<InventarioAreaAgrupadoResponseDTO>> listarAgrupadoPorArea(
            @PathVariable Integer areaId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(inventarioAreaService.listarAgrupadoPorArea(areaId, pageable));
    }

    @PostMapping("/transferir")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA','SUPERVISOR_SST')")
    @Operation(summary = "Transferir stock de central a área")
    public ResponseEntity<InventarioAreaResponseDTO> transferir(@Valid @RequestBody TransferenciaStockDTO request) {
        return ResponseEntity.ok(inventarioAreaService.transferirStockCentralAArea(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA','SUPERVISOR_SST')")
    public ResponseEntity<InventarioAreaResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody InventarioAreaUpdateDTO request) {
        return ResponseEntity.ok(inventarioAreaService.actualizar(id, request));
    }

    @GetMapping("/alertas/stock-critico")
    @Operation(summary = "Stock bajo global en todas las áreas")
    public ResponseEntity<List<InventarioAreaResponseDTO>> stockCritico() {
        return ResponseEntity.ok(inventarioAreaService.listarStockBajoGlobal());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        inventarioAreaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
