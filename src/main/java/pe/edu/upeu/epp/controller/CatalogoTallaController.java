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
import pe.edu.upeu.epp.dto.request.CatalogoTallaRequestDTO;
import pe.edu.upeu.epp.dto.response.CatalogoTallaResponseDTO;
import pe.edu.upeu.epp.service.CatalogoTallaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tallas")
@RequiredArgsConstructor
@Tag(name = "Tallas", description = "Gestión del catálogo maestro de tallas")
@SecurityRequirement(name = "Bearer Authentication")
public class CatalogoTallaController {

    private final CatalogoTallaService tallaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Crear talla", description = "Agrega una nueva talla al catálogo maestro")
    public ResponseEntity<CatalogoTallaResponseDTO> crear(@Valid @RequestBody CatalogoTallaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tallaService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las tallas", description = "Retorna el catálogo maestro de tallas ordenado")
    public ResponseEntity<List<CatalogoTallaResponseDTO>> listar() {
        return ResponseEntity.ok(tallaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener talla por ID")
    public ResponseEntity<CatalogoTallaResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(tallaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Actualizar talla", description = "Edita el nombre, descripción y orden de visualización de una talla existente")
    public ResponseEntity<CatalogoTallaResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CatalogoTallaRequestDTO request) {
        return ResponseEntity.ok(tallaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar talla")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tallaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
