package pe.edu.upeu.epp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.service.AzureStorageService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test-azure")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Azure", description = "Endpoint temporal para debugear subida de archivos")
public class TestAzureController {

    private final AzureStorageService azureStorageService;

    // Permitimos acceso sin token para no complicar el debug (temporalmente)
    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> probarSubida(
            @RequestPart("archivo") MultipartFile archivo) {

        log.info("Recibiendo petición de prueba para archivo: {}", archivo.getOriginalFilename());

        try {
            String url = azureStorageService.subirArchivo(archivo);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "¡Subida exitosa!",
                    "urlAzure", url
            ));
        } catch (Exception e) {
            log.error("Error fatal en el controlador de prueba", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "causa", e.getCause() != null ? e.getCause().getMessage() : "Desconocida"
            ));
        }
    }
}