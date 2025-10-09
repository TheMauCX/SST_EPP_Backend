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
 * Controller REST para gestión del Catálogo de EPP.
 * Expone endpoints CRUD para administración de tipos de EPP.
 */
@RestController
@RequestMapping("/api/v1/catalogo-epp")
@RequiredArgsConstructor
@Tag(name = "Catálogo EPP", description = "Gestión del catálogo de Equipos de Protección Personal")
@SecurityRequirement(name = "Bearer Authentication")
public class CatalogoEppController {

    private final CatalogoEppService catalogoEppService;

    /**
     * Crear un nuevo EPP en el catálogo.
     * Solo accesible por ADMINISTRADOR_SISTEMA.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Crear nuevo EPP", description = "Crea un nuevo tipo de EPP en el catálogo")
    public ResponseEntity<CatalogoEppResponseDTO> crear(
            @Valid @RequestBody CatalogoEppRequestDTO request) {
        CatalogoEppResponseDTO response = catalogoEppService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un EPP por ID.
     * Accesible por todos los roles autenticados.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener EPP por ID", description = "Retorna los detalles de un EPP específico")
    public ResponseEntity<CatalogoEppResponseDTO> obtenerPorId(
            @Parameter(description = "ID del EPP") @PathVariable Integer id) {
        CatalogoEppResponseDTO response = catalogoEppService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los EPPs con paginación.
     * Accesible por todos los roles autenticados.
     */
    @GetMapping
    @Operation(summary = "Listar todos los EPPs", description = "Retorna una lista paginada de todos los EPPs")
    public ResponseEntity<Page<CatalogoEppResponseDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "nombreEpp", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CatalogoEppResponseDTO> response = catalogoEppService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar solo EPPs activos.
     * Accesible por todos los roles autenticados.
     */
    @GetMapping("/activos")
    @Operation(summary = "Listar EPPs activos", description = "Retorna solo los EPPs que están activos")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarActivos() {
        List<CatalogoEppResponseDTO> response = catalogoEppService.listarActivos();
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar EPPs por nombre.
     * Accesible por todos los roles autenticados.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar EPPs por nombre", description = "Busca EPPs cuyo nombre contenga el texto especificado")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre")
            @RequestParam String nombre) {
        List<CatalogoEppResponseDTO> response = catalogoEppService.buscarPorNombre(nombre);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar EPPs por tipo de uso.
     * Accesible por todos los roles autenticados.
     */
    @GetMapping("/tipo/{tipoUso}")
    @Operation(summary = "Listar EPPs por tipo", description = "Retorna EPPs filtrados por tipo de uso (CONSUMIBLE o DURADERO)")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarPorTipo(
            @Parameter(description = "Tipo de uso: CONSUMIBLE o DURADERO")
            @PathVariable CatalogoEpp.TipoUso tipoUso) {
        List<CatalogoEppResponseDTO> response = catalogoEppService.listarPorTipo(tipoUso);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un EPP existente.
     * Solo accesible por ADMINISTRADOR_SISTEMA.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Actualizar EPP", description = "Actualiza la información de un EPP existente")
    public ResponseEntity<CatalogoEppResponseDTO> actualizar(
            @Parameter(description = "ID del EPP") @PathVariable Integer id,
            @Valid @RequestBody CatalogoEppUpdateDTO request) {
        CatalogoEppResponseDTO response = catalogoEppService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar (desactivar) un EPP.
     * Solo accesible por ADMINISTRADOR_SISTEMA.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar EPP", description = "Desactiva un EPP (no lo elimina físicamente)")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del EPP") @PathVariable Integer id) {
        catalogoEppService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}