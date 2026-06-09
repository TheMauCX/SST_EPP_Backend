package pe.edu.upeu.epp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.request.NormaEppRequestDTO;
import pe.edu.upeu.epp.dto.response.NormaEppResponseDTO;
import pe.edu.upeu.epp.service.NormaEppService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/normas")
@RequiredArgsConstructor
@Tag(name = "Normas EPP",
     description = "Catálogo de normas de seguridad (ANSI, ISO, NTP, EN…). " +
                   "Cada norma tiene código, descripción detallada e ícono para la UI.")
@SecurityRequirement(name = "Bearer Authentication")
public class NormaEppController {

    private final NormaEppService normaEppService;

    @GetMapping
    @Operation(summary = "Listar normas activas",
               description = "Devuelve todas las normas activas. Usar para poblar el selector múltiple en el formulario de EPP.")
    public ResponseEntity<List<NormaEppResponseDTO>> listar() {
        return ResponseEntity.ok(normaEppService.listarActivas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de norma",
               description = "Incluye la descripción completa para el panel desplegable en la UI.")
    public ResponseEntity<NormaEppResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(normaEppService.obtenerPorId(id));
    }

    @GetMapping("/organismo/{organismo}")
    @Operation(summary = "Filtrar por organismo",
               description = "Valores: ANSI, ISO, NTP, NIOSH, EN, OSHA, GB, AS_NZS, OTRO")
    public ResponseEntity<List<NormaEppResponseDTO>> porOrganismo(@PathVariable String organismo) {
        return ResponseEntity.ok(normaEppService.listarPorOrganismo(organismo));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Filtrar por categoría de protección",
               description = "Valores: CABEZA, OJOS, OIDOS, RESPIRATORIA, MANOS, PIES, CAIDAS, CUERPO, SENALIZACION, MULTIPLE")
    public ResponseEntity<List<NormaEppResponseDTO>> porCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(normaEppService.listarPorCategoria(categoria));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Crear norma (ADMIN)")
    public ResponseEntity<NormaEppResponseDTO> crear(@Valid @RequestBody NormaEppRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(normaEppService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Actualizar norma (ADMIN)")
    public ResponseEntity<NormaEppResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody NormaEppRequestDTO request) {
        return ResponseEntity.ok(normaEppService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Desactivar norma (ADMIN)", description = "Soft delete — no elimina de BD.")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        normaEppService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
