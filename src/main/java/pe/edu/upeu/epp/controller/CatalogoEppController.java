package pe.edu.upeu.epp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.dto.request.CatalogoEppRequestDTO;
import pe.edu.upeu.epp.dto.request.CatalogoEppUpdateDTO;
import pe.edu.upeu.epp.dto.response.CatalogoEppResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.service.CatalogoEppService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogo-epp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Catálogo EPP", description = "Gestión del catálogo de Equipos de Protección Personal")
@SecurityRequirement(name = "Bearer Authentication")
public class CatalogoEppController {

    private final CatalogoEppService catalogoEppService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Crear nuevo EPP")
    public ResponseEntity<?> crear(
            @RequestPart("eppData") String eppDataJson,
            @RequestPart(value = "fotoArchivo", required = false) MultipartFile fotoArchivo) {
        try {
            CatalogoEppRequestDTO request = new ObjectMapper().readValue(eppDataJson, CatalogoEppRequestDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(catalogoEppService.crear(request, fotoArchivo));
        } catch (Exception e) {
            log.error("Error al crear EPP: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Actualizar EPP")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @RequestPart("eppData") String eppDataJson,
            @RequestPart(value = "fotoArchivo", required = false) MultipartFile fotoArchivo) {
        try {
            CatalogoEppUpdateDTO request = new ObjectMapper().readValue(eppDataJson, CatalogoEppUpdateDTO.class);
            return ResponseEntity.ok(catalogoEppService.actualizar(id, request, fotoArchivo));
        } catch (Exception e) {
            log.error("Error al actualizar EPP: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * HU-19: Subir o reemplazar la ficha técnica PDF de un EPP.
     * El archivo debe ser .pdf y pesar menos de 5 MB.
     */
    @PostMapping(value = "/{id}/ficha-tecnica", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Subir ficha técnica PDF",
            description = "Adjunta o reemplaza el archivo PDF de la ficha técnica de un EPP. " +
                    "Máximo 5 MB, solo formato PDF.")
    public ResponseEntity<?> subirFichaTecnica(
            @Parameter(description = "ID del EPP") @PathVariable Integer id,
            @RequestPart("fichaTecnica") MultipartFile pdfFile) {
        try {
            CatalogoEppResponseDTO response = catalogoEppService.subirFichaTecnica(id, pdfFile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al subir ficha técnica para EPP {}: ", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener EPP por ID")
    public ResponseEntity<CatalogoEppResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoEppService.obtenerPorId(id));
    }

    /**
     * HU-23: Listar EPPs con filtro por rotación.
     * Parámetro ?rotacion=alta|nula   (omitir para ver todos)
     */
    @GetMapping
    @Operation(
            summary = "Listar EPPs",
            description = "Lista paginada de EPPs del catálogo. " +
                    "Filtros: rotacion=alta (con salidas recientes) | rotacion=nula (inmovilizados)")
    public ResponseEntity<Page<CatalogoEppResponseDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "nombreEpp", direction = Sort.Direction.ASC) Pageable pageable,
            @Parameter(description = "Filtro por rotación: alta | nula")
            @RequestParam(required = false) String rotacion) {
        return ResponseEntity.ok(catalogoEppService.listar(pageable, rotacion));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar EPPs activos (sin paginar)")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarActivos() {
        return ResponseEntity.ok(catalogoEppService.listarActivos());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar EPPs por nombre")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(catalogoEppService.buscarPorNombre(nombre));
    }

    @GetMapping("/fabricante")
    @Operation(summary = "Buscar EPPs por fabricante")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorFabricante(@RequestParam String fabricante) {
        return ResponseEntity.ok(catalogoEppService.buscarPorFabricante(fabricante));
    }

    @GetMapping("/norma")
    @Operation(summary = "Buscar EPPs por norma")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorNorma(@RequestParam String norma) {
        return ResponseEntity.ok(catalogoEppService.buscarPorNorma(norma));
    }

    @GetMapping("/tipo/{tipoUso}")
    @Operation(summary = "Listar EPPs por tipo de uso")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarPorTipo(@PathVariable CatalogoEpp.TipoUso tipoUso) {
        return ResponseEntity.ok(catalogoEppService.listarPorTipo(tipoUso));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Desactivar EPP")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        catalogoEppService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
