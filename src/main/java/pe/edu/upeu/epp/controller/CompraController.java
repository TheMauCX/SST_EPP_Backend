package pe.edu.upeu.epp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.dto.request.CompraRequestDTO;
import pe.edu.upeu.epp.entity.Compra;
import pe.edu.upeu.epp.service.CompraService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Compras", description = "Gestión de compras e ingresos directos a Inventario Central")
@SecurityRequirement(name = "Bearer Authentication")
public class CompraController {

    private final CompraService compraService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(summary = "Registrar nueva compra", description = "Registra una factura, sube el archivo PDF/JPG, y suma automáticamente el stock al Inventario Central.")
    public ResponseEntity<?> registrarCompra(
            @RequestPart("compraData") String compraDataJson,
            @RequestPart(value = "facturaArchivo", required = false) MultipartFile facturaArchivo,
            Authentication authentication) {

        log.info("====== INICIANDO ENDPOINT DE COMPRAS ======");
        log.info("JSON Recibido: {}", compraDataJson);

        // DEBUG DEL ARCHIVO
        if (facturaArchivo == null) {
            log.warn("⚠️ ALERTA: El parámetro 'facturaArchivo' llegó como NULL. Verifica el nombre de la Key en Postman.");
        } else if (facturaArchivo.isEmpty()) {
            log.warn("⚠️ ALERTA: El archivo llegó pero pesa 0 bytes.");
        } else {
            log.info("✅ Archivo recibido correctamente. Nombre: {}, Tamaño: {} bytes",
                    facturaArchivo.getOriginalFilename(), facturaArchivo.getSize());
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            CompraRequestDTO requestDTO = mapper.readValue(compraDataJson, CompraRequestDTO.class);

            String username = authentication.getName();
            log.info("Usuario autenticado: {}", username);

            Compra compraGuardada = compraService.registrarCompra(requestDTO, facturaArchivo, username);

            log.info("====== COMPRA EXITOSA ID: {} ======", compraGuardada.getCompraId());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "mensaje", "Compra registrada e inventario actualizado exitosamente",
                            "compraId", compraGuardada.getCompraId(),
                            "urlFactura", compraGuardada.getRutaArchivoFactura() != null ? compraGuardada.getRutaArchivoFactura() : "No se subió archivo",
                            "totalGastado", compraGuardada.getMontoTotal()
                    )
            );
        } catch (Exception e) {
            log.error("❌ ERROR EN EL CONTROLADOR DE COMPRAS: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}