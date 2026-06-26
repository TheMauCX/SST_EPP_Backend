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
import pe.edu.upeu.epp.dto.response.*;
import pe.edu.upeu.epp.service.ReporteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes y Dashboard",
     description = "Indicadores financieros, estadísticas de consumo y análisis de inventario EPP")
@SecurityRequirement(name = "Bearer Authentication")
public class ReporteController {

    private final ReporteService reporteService;

    // ── HU-14: Dashboard de gastos ───────────────────────────────────────

    @GetMapping("/gastos")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Gastos mensuales",
            description = "Gasto mensual en EPPs = SUM(cantidad_entregada × precio_unitario) por mes.")
    public ResponseEntity<DashboardGastosResponseDTO> dashboardGastos(
            @RequestParam(required = false) Integer areaId,
            @RequestParam(defaultValue = "12") int meses) {
        return ResponseEntity.ok(reporteService.calcularGastosMensuales(areaId, meses));
    }

    // ── HU-15: Rotación ──────────────────────────────────────────────────

    @GetMapping("/rotacion")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Rotación y EPPs inmovilizados",
            description = "Top EPPs más entregados y EPPs sin salidas en el período.")
    public ResponseEntity<RotacionConsumoResponseDTO> analisisRotacion(
            @RequestParam(defaultValue = "3") int meses) {
        return ResponseEntity.ok(reporteService.calcularRotacion(meses));
    }

    // ── HU-6: Ficha de trabajador ────────────────────────────────────────

    @GetMapping("/trabajadores/{trabajadorId}/ficha")
    @Operation(summary = "Ficha de EPPs de un trabajador (JSON)")
    public ResponseEntity<FichaTrabajadorResponseDTO> fichaTrabajador(
            @PathVariable Integer trabajadorId) {
        return ResponseEntity.ok(reporteService.generarFichaTrabajador(trabajadorId));
    }

    @GetMapping(value = "/trabajadores/{trabajadorId}/ficha/pdf",
                produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Ficha de EPPs de un trabajador (PDF descargable)")
    public ResponseEntity<byte[]> fichaTrabajadorPdf(@PathVariable Integer trabajadorId) {
        byte[] pdf = reporteService.generarFichaTrabajadorPdf(trabajadorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ficha-trabajador-" + trabajadorId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ESTADÍSTICAS NUEVAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * A — Precio unitario actual de todos los EPPs.
     * Ideal para alimentar un gráfico de barras comparativo.
     * Retorna lista ordenada de mayor a menor precio.
     */
    @GetMapping("/estadisticas/precios-epp")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Precios actuales de todos los EPPs",
            description = "Devuelve el precio unitario del lote más reciente de cada EPP. " +
                    "Ordenado de mayor a menor. Usar para gráfico de barras comparativo.")
    public ResponseEntity<List<PrecioEppDTO>> preciosActuales() {
        return ResponseEntity.ok(reporteService.obtenerPreciosActuales());
    }

    /**
     * B — Historial de precios de un EPP específico.
     * Cada punto representa una compra distinta (lote diferente).
     */
    @GetMapping("/estadisticas/historial-precios/{eppId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Historial de precios de un EPP",
            description = "Muestra cómo ha evolucionado el precio unitario de un EPP " +
                    "en cada compra registrada. Incluye mín, máx, promedio y precio actual.")
    public ResponseEntity<HistorialPreciosEppDTO> historialPrecios(
            @Parameter(description = "ID del EPP") @PathVariable Integer eppId) {
        return ResponseEntity.ok(reporteService.obtenerHistorialPrecios(eppId));
    }

    /**
     * C — Valor monetario del inventario central.
     * Cuánto dinero está inmovilizado en stock (cantidad × costo unitario).
     */
    @GetMapping("/estadisticas/valor-inventario")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Valorización del inventario central",
            description = "Calcula el valor total del stock actual: SUM(cantidad × costo_unitario) " +
                    "por cada EPP. Incluye porcentaje de cada EPP sobre el total.")
    public ResponseEntity<ValorInventarioDTO> valorInventario() {
        return ResponseEntity.ok(reporteService.calcularValorInventario());
    }

    /**
     * D — Trabajadores con mayor valor acumulado de EPPs recibidos.
     * Permite identificar quién consume más presupuesto.
     */
    @GetMapping("/estadisticas/trabajadores-mayor-gasto")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Trabajadores que más gastan en EPPs",
            description = "Ranking de trabajadores por el valor total de EPPs recibidos " +
                    "(cantidad × precio unitario). Parámetro 'top' limita el número de resultados.")
    public ResponseEntity<TrabajadorMayorGastoDTO> trabajadoresMayorGasto(
            @Parameter(description = "Número de trabajadores a retornar (default: 10)")
            @RequestParam(defaultValue = "10") int top) {
        return ResponseEntity.ok(reporteService.calcularTrabajadoresMayorGasto(top));
    }

    /**
     * E — EPPs más comprados: frecuencia en facturas.
     * Diferente a rotación — mide compras, no entregas.
     */
    @GetMapping("/estadisticas/frecuencia-compras")
    @PreAuthorize("hasAnyRole('SUPERVISOR_SST', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "EPPs más comprados (frecuencia en facturas)",
            description = "Muestra cuántas veces aparece cada EPP en facturas de compra, " +
                    "cuántas unidades se compraron en total y el gasto histórico acumulado.")
    public ResponseEntity<FrecuenciaComprasDTO> frecuenciaCompras() {
        return ResponseEntity.ok(reporteService.calcularFrecuenciaCompras());
    }
}
