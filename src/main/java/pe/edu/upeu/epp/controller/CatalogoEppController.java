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
import pe.edu.upeu.epp.dto.request.CatalogoEppRequestDTO;
import pe.edu.upeu.epp.dto.request.CatalogoEppUpdateDTO;
import pe.edu.upeu.epp.dto.response.CatalogoEppResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.service.CatalogoEppService;

import java.util.List;

/**
 * Controller REST para gestión del Catálogo de EPP (Ficha Técnica).
 */
@RestController
@RequestMapping("/api/v1/catalogo-epp")
@RequiredArgsConstructor
@Tag(name = "Catálogo EPP", description = "Gestión del catálogo y fichas técnicas de Equipos de Protección Personal")
@SecurityRequirement(name = "Bearer Authentication")
public class CatalogoEppController {

    private final CatalogoEppService catalogoEppService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Crear nuevo EPP", description = "Crea un nuevo tipo de EPP en el catálogo")
    public ResponseEntity<CatalogoEppResponseDTO> crear(@Valid @RequestBody CatalogoEppRequestDTO request) {
        CatalogoEppResponseDTO response = catalogoEppService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener EPP por ID", description = "Retorna los detalles de un EPP específico")
    public ResponseEntity<CatalogoEppResponseDTO> obtenerPorId(@Parameter(description = "ID del EPP") @PathVariable Integer id) {
        CatalogoEppResponseDTO response = catalogoEppService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos los EPPs", description = "Retorna una lista de todos los EPPs")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarTodos() {
        List<CatalogoEppResponseDTO> response = catalogoEppService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar EPPs activos", description = "Retorna solo los EPPs que están activos")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarActivos() {
        List<CatalogoEppResponseDTO> response = catalogoEppService.listarActivos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar EPPs por nombre", description = "Busca EPPs cuyo nombre contenga el texto especificado")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<CatalogoEppResponseDTO> response = catalogoEppService.buscarPorNombre(nombre);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fabricante")
    @Operation(summary = "Buscar EPPs por Fabricante", description = "Busca EPPs pertenecientes a un fabricante específico")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorFabricante(@RequestParam String fabricante) {
        List<CatalogoEppResponseDTO> response = catalogoEppService.buscarPorFabricante(fabricante);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/norma")
    @Operation(summary = "Buscar EPPs por Norma", description = "Busca EPPs que cumplan con cierta norma o aprobación (ej. ANSI)")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorNorma(@RequestParam String norma) {
        List<CatalogoEppResponseDTO> response = catalogoEppService.buscarPorNorma(norma);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tipo/{tipoUso}")
    @Operation(summary = "Listar EPPs por tipo", description = "Retorna EPPs filtrados por tipo de uso (CONSUMIBLE o DURADERO)")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarPorTipo(@PathVariable CatalogoEpp.TipoUso tipoUso) {
        List<CatalogoEppResponseDTO> response = catalogoEppService.listarPorTipo(tipoUso);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Actualizar EPP", description = "Actualiza la información (Ficha Técnica) de un EPP existente")
    public ResponseEntity<CatalogoEppResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody CatalogoEppUpdateDTO request) {
        CatalogoEppResponseDTO response = catalogoEppService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar EPP", description = "Desactiva un EPP (no lo elimina físicamente)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        catalogoEppService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}