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
@Tag(name = "Compras", description = "Registro de compras con factura y cotización opcional")
@SecurityRequirement(name = "Bearer Authentication")
public class CompraController {

    private final CompraService compraService;

    /**
     * Registra una compra.
     *
     * Multipart keys:
     *   compraData        → JSON obligatorio
     *   facturaArchivo    → archivo de factura (opcional)
     *   cotizacionArchivo → archivo de cotización (opcional, sprint 4b)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'SUPERVISOR_SST')")
    @Operation(
            summary = "Registrar compra",
            description = "Registra una factura, actualiza el inventario central. " +
                    "Acepta opcionalmente el archivo de factura y/o cotización.")
    public ResponseEntity<?> registrarCompra(
            @RequestPart("compraData") String compraDataJson,
            @RequestPart(value = "facturaArchivo",    required = false) MultipartFile facturaArchivo,
            @RequestPart(value = "cotizacionArchivo", required = false) MultipartFile cotizacionArchivo,
            Authentication authentication) {

        log.info("POST /compras — usuario: {}", authentication.getName());
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            CompraRequestDTO requestDTO = mapper.readValue(compraDataJson, CompraRequestDTO.class);

            Compra compra = compraService.registrarCompra(
                    requestDTO, facturaArchivo, cotizacionArchivo, authentication.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje",       "Compra registrada e inventario actualizado exitosamente",
                    "compraId",      compra.getCompraId(),
                    "urlFactura",    compra.getRutaArchivoFactura()   != null ? compra.getRutaArchivoFactura()   : "No se subió factura",
                    "urlCotizacion", compra.getRutaArchivoCotizacion() != null ? compra.getRutaArchivoCotizacion() : "No se subió cotización",
                    "subtotal",      compra.getSubtotal(),
                    "igv",           compra.getIgv(),
                    "totalGastado",  compra.getMontoTotal()
            ));
        } catch (Exception e) {
            log.error("Error en registro de compra: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
