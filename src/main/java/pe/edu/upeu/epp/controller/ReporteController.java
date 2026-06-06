package pe.edu.upeu.epp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.response.DashboardGastosResponseDTO;
import pe.edu.upeu.epp.dto.response.FichaTrabajadorResponseDTO;
import pe.edu.upeu.epp.dto.response.RotacionConsumoResponseDTO;
import pe.edu.upeu.epp.service.ReporteService;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes y Dashboard", description = "Indicadores financieros y análisis de consumo de EPPs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'COORDINADOR_SST', 'ADMINISTRADOR_SISTEMA')")
public class ReporteController {

    private final ReporteService reporteService;

    // ── HU-14: Dashboard de gastos ───────────────────────────────────────

    /**
     * Retorna el gasto mensual en EPPs calculado como:
     * SUM(cantidad_entregada × costo_unitario) agrupado por mes.
     *
     * @param areaId  Filtro opcional por área. Omitir para ver todas las áreas.
     * @param meses   Cantidad de meses hacia atrás a incluir (default: 12).
     */
    @GetMapping("/gastos")
    @Operation(
            summary = "Dashboard de gastos por mes",
            description = "Retorna el array de gasto mensual para alimentar el gráfico de barras. " +
                    "Fórmula: cantidad_entregada × precio_unitario de la última compra del EPP.")
    public ResponseEntity<DashboardGastosResponseDTO> dashboardGastos(
            @Parameter(description = "Filtrar por ID de área (opcional)")
            @RequestParam(required = false) Integer areaId,
            @Parameter(description = "Meses hacia atrás (default 12)")
            @RequestParam(defaultValue = "12") int meses) {
        return ResponseEntity.ok(reporteService.calcularGastosMensuales(areaId, meses));
    }

    // ── HU-15: Rotación y consumo ─────────────────────────────────────────

    /**
     * Retorna dos listas:
     *   - mayorRotacion: top EPPs más entregados en el período
     *   - inmovilizados: EPPs con cero salidas en el período
     *
     * @param meses Período de análisis en meses hacia atrás (default: 3).
     */
    @GetMapping("/rotacion")
    @Operation(
            summary = "Análisis de rotación y consumo",
            description = "Devuelve los EPPs con mayor rotación y los inmovilizados " +
                    "para optimizar futuras compras.")
    public ResponseEntity<RotacionConsumoResponseDTO> analisisRotacion(
            @Parameter(description = "Período en meses hacia atrás (default 3)")
            @RequestParam(defaultValue = "3") int meses) {
        return ResponseEntity.ok(reporteService.calcularRotacion(meses));
    }

    // ── HU-6: Ficha de trabajador ─────────────────────────────────────────

    /**
     * Retorna el historial completo de EPPs entregados a un trabajador.
     * Incluye: EPP, talla, cantidad, motivo, fecha, quién entregó.
     */
    @GetMapping("/trabajadores/{trabajadorId}/ficha")
    @Operation(
            summary = "Ficha de EPPs de un trabajador",
            description = "Lista completa del historial de entregas de EPP a un trabajador. " +
                    "Accesible también desde TrabajadorController como alias.")
    public ResponseEntity<FichaTrabajadorResponseDTO> fichaTrabajador(
            @Parameter(description = "ID del trabajador") @PathVariable Integer trabajadorId) {
        return ResponseEntity.ok(reporteService.generarFichaTrabajador(trabajadorId));
    }

    /**
     * HU-6: Descarga la ficha del trabajador en formato PDF.
     * El PDF se genera en tiempo de ejecución con Apache PDFBox.
     */
    @GetMapping(value = "/trabajadores/{trabajadorId}/ficha/pdf",
                produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Descargar ficha como PDF",
            description = "Genera y descarga el PDF de la ficha de EPPs de un trabajador.")
    public ResponseEntity<byte[]> fichaTrabajadorPdf(
            @Parameter(description = "ID del trabajador") @PathVariable Integer trabajadorId) {

        byte[] pdf = reporteService.generarFichaTrabajadorPdf(trabajadorId);
        String filename = "ficha-trabajador-" + trabajadorId + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
