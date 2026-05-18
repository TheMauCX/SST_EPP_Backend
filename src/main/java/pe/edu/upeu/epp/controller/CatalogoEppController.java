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
@Tag(name = "Catálogo EPP", description = "Gestión del catálogo y fichas técnicas de Equipos de Protección Personal")
@SecurityRequirement(name = "Bearer Authentication")
public class CatalogoEppController {

    private final CatalogoEppService catalogoEppService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Crear nuevo EPP", description = "Crea un nuevo tipo de EPP en el catálogo adjuntando su foto")
    public ResponseEntity<?> crear(
            @RequestPart("eppData") String eppDataJson,
            @RequestPart(value = "fotoArchivo", required = false) MultipartFile fotoArchivo) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            CatalogoEppRequestDTO request = mapper.readValue(eppDataJson, CatalogoEppRequestDTO.class);

            CatalogoEppResponseDTO response = catalogoEppService.crear(request, fotoArchivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error al crear EPP en catálogo: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Actualizar EPP", description = "Actualiza la Ficha Técnica de un EPP y opcionalmente su foto")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @RequestPart("eppData") String eppDataJson,
            @RequestPart(value = "fotoArchivo", required = false) MultipartFile fotoArchivo) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            CatalogoEppUpdateDTO request = mapper.readValue(eppDataJson, CatalogoEppUpdateDTO.class);

            CatalogoEppResponseDTO response = catalogoEppService.actualizar(id, request, fotoArchivo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al actualizar EPP en catálogo: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // --- EL RESTO DE ENDPOINTS SE MANTIENEN INTACTOS ---

    @GetMapping("/{id}")
    @Operation(summary = "Obtener EPP por ID")
    public ResponseEntity<CatalogoEppResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoEppService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos los EPPs")
    public ResponseEntity<Page<CatalogoEppResponseDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "nombreEpp", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(catalogoEppService.listarTodos(pageable));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar EPPs activos")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarActivos() {
        return ResponseEntity.ok(catalogoEppService.listarActivos());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar EPPs por nombre")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(catalogoEppService.buscarPorNombre(nombre));
    }

    @GetMapping("/fabricante")
    @Operation(summary = "Buscar EPPs por Fabricante")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorFabricante(@RequestParam String fabricante) {
        return ResponseEntity.ok(catalogoEppService.buscarPorFabricante(fabricante));
    }

    @GetMapping("/norma")
    @Operation(summary = "Buscar EPPs por Norma")
    public ResponseEntity<List<CatalogoEppResponseDTO>> buscarPorNorma(@RequestParam String norma) {
        return ResponseEntity.ok(catalogoEppService.buscarPorNorma(norma));
    }

    @GetMapping("/tipo/{tipoUso}")
    @Operation(summary = "Listar EPPs por tipo")
    public ResponseEntity<List<CatalogoEppResponseDTO>> listarPorTipo(@PathVariable CatalogoEpp.TipoUso tipoUso) {
        return ResponseEntity.ok(catalogoEppService.listarPorTipo(tipoUso));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar EPP (Desactivar)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        catalogoEppService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}