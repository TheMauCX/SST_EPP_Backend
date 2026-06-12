package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.response.*;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de reportes y estadísticas.
 *
 * HU-6:  Ficha de trabajador (JSON + PDF)
 * HU-14: Gastos mensuales
 * HU-15: Rotación y EPPs inmovilizados
 *
 * Sprint estadísticas (nuevos):
 *   A — Precios actuales de todos los EPPs (gráfico de barras)
 *   B — Historial de precios de un EPP específico
 *   C — Valor monetario del inventario central
 *   D — Trabajadores con mayor gasto acumulado
 *   E — EPPs más comprados (frecuencia en facturas)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final TrabajadorRepository              trabajadorRepository;
    private final EntregaEppRepository              entregaEppRepository;
    private final DetalleEntregaEppRepository       detalleEntregaEppRepository;
    private final InventarioCentralRepository       inventarioCentralRepository;
    private final DetalleCompraRepository           detalleCompraRepository;
    private final CatalogoEppRepository             catalogoEppRepository;
    private final TemplateEngine                    templateEngine;

    // ════════════════════════════════════════════════════════════════════════
    // HU-6: Ficha de trabajador
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public FichaTrabajadorResponseDTO generarFichaTrabajador(Integer trabajadorId) {
        log.info("Generando ficha de trabajador ID: {}", trabajadorId);

        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trabajador no encontrado con ID: " + trabajadorId));

        List<EntregaEpp> entregas = entregaEppRepository.findByTrabajador(trabajador);

        List<FichaTrabajadorResponseDTO.EntregaFichaDTO> entregasDTO = entregas.stream()
                .map(entrega -> {
                    List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entrega);

                    List<FichaTrabajadorResponseDTO.ItemFichaDTO> items = detalles.stream()
                            .map(detalle -> {
                                BigDecimal costoUnitario = obtenerCostoUnitario(detalle.getEpp());
                                BigDecimal subtotal = costoUnitario != null && detalle.getCantidad() != null
                                        ? costoUnitario.multiply(BigDecimal.valueOf(detalle.getCantidad()))
                                        : BigDecimal.ZERO;
                                return FichaTrabajadorResponseDTO.ItemFichaDTO.builder()
                                        .nombreEpp(detalle.getEpp().getNombreEpp())
                                        .talla(detalle.getTalla() != null ? detalle.getTalla().getNombre() : null)
                                        .cantidad(detalle.getCantidad())
                                        .motivo(detalle.getMotivo())
                                        .costoUnitario(costoUnitario)
                                        .subtotalItem(subtotal)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    String entregadoPor = null;
                    if (entrega.getSupervisorUsuario() != null) {
                        Usuario s = entrega.getSupervisorUsuario();
                        entregadoPor = (s.getNombreCompleto() != null && !s.getNombreCompleto().isBlank())
                                ? s.getNombreCompleto() : s.getNombreUsuario();
                    }

                    return FichaTrabajadorResponseDTO.EntregaFichaDTO.builder()
                            .entregaId(entrega.getEntregaId())
                            .fechaEntrega(entrega.getFechaEntrega())
                            .tipoEntrega(entrega.getTipoEntrega())
                            .observaciones(entrega.getObservaciones())
                            .entregadoPor(entregadoPor)
                            .items(items)
                            .build();
                })
                .collect(Collectors.toList());

        return FichaTrabajadorResponseDTO.builder()
                .trabajadorId(trabajador.getTrabajadorId())
                .nombreCompleto(trabajador.getNombres() + " " + trabajador.getApellidos())
                .dni(trabajador.getDni())
                .cargo(trabajador.getCargo())
                .areaNombre(trabajador.getArea() != null ? trabajador.getArea().getNombreArea() : null)
                .entregas(entregasDTO)
                .totalEntregas(entregasDTO.size())
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] generarFichaTrabajadorPdf(Integer trabajadorId) {
        FichaTrabajadorResponseDTO ficha = generarFichaTrabajador(trabajadorId);

        Context context = new Context();
        context.setVariable("ficha", ficha);

        String html = templateEngine.process("ficha-trabajador", context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar el PDF: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HU-14: Gastos mensuales
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public DashboardGastosResponseDTO calcularGastosMensuales(Integer areaId, int meses) {
        List<Object[]> rawData = inventarioCentralRepository.calcularGastoMensual(areaId);
        List<GastoMensualDTO> gastos = rawData.stream()
                .map(row -> {
                    int anio = ((Number) row[0]).intValue();
                    int mes  = ((Number) row[1]).intValue();
                    BigDecimal total = row[2] != null
                            ? new BigDecimal(row[2].toString()).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    String nombre = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "PE"));
                    return GastoMensualDTO.builder().anio(anio).mes(mes)
                            .mesNombre(nombre.substring(0, 1).toUpperCase() + nombre.substring(1))
                            .totalGasto(total).build();
                })
                .collect(Collectors.toList());
        BigDecimal total = gastos.stream().map(GastoMensualDTO::getTotalGasto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return DashboardGastosResponseDTO.builder()
                .gastosMensuales(gastos).totalAcumulado(total)
                .filtroArea(areaId != null ? "Área ID " + areaId : "Todas las áreas").build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HU-15: Rotación
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public RotacionConsumoResponseDTO calcularRotacion(int meses) {
        LocalDateTime desde = LocalDateTime.now().minusMonths(meses);
        List<EppRotacionDTO> mayorRotacion = inventarioCentralRepository.findEppsMasEntregados(desde)
                .stream().map(r -> EppRotacionDTO.builder()
                        .eppId(((Number) r[0]).intValue()).nombreEpp((String) r[1])
                        .totalEntregado(((Number) r[2]).longValue()).build())
                .collect(Collectors.toList());
        List<EppInmovilizadoDTO> inmovilizados = inventarioCentralRepository.findEppsInmovilizados(desde)
                .stream().map(r -> EppInmovilizadoDTO.builder()
                        .eppId(((Number) r[0]).intValue()).nombreEpp((String) r[1]).build())
                .collect(Collectors.toList());
        return RotacionConsumoResponseDTO.builder()
                .mayorRotacion(mayorRotacion).inmovilizados(inmovilizados)
                .periodoDesde(desde.toString().substring(0, 10)).build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // ESTADÍSTICAS NUEVAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * A — Precio unitario actual de todos los EPPs.
     * Retorna lista ordenada de mayor a menor precio → gráfico de barras frontend.
     */
    @Transactional(readOnly = true)
    public List<PrecioEppDTO> obtenerPreciosActuales() {
        log.info("Obteniendo precios actuales de todos los EPPs");
        return inventarioCentralRepository.findPrecioActualPorEpp().stream()
                .map(row -> PrecioEppDTO.builder()
                        .eppId(((Number) row[0]).intValue())
                        .eppNombre((String) row[1])
                        .costoUnitario(row[2] != null
                                ? new BigDecimal(row[2].toString()).setScale(2, RoundingMode.HALF_UP)
                                : null)
                        .fechaAdquisicion(row[3] != null ? toLocalDate(row[3]) : null)
                        .proveedor(row[4] != null ? (String) row[4] : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * B — Historial de precios de un EPP específico a lo largo del tiempo.
     */
    @Transactional(readOnly = true)
    public HistorialPreciosEppDTO obtenerHistorialPrecios(Integer eppId) {
        log.info("Obteniendo historial de precios para EPP ID: {}", eppId);

        CatalogoEpp epp = catalogoEppRepository.findById(eppId)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + eppId));

        List<Object[]> raw = inventarioCentralRepository.findHistorialPreciosByEpp(eppId);

        List<HistorialPreciosEppDTO.PuntoHistorialDTO> historial = raw.stream()
                .map(row -> HistorialPreciosEppDTO.PuntoHistorialDTO.builder()
                        .lote(row[0] != null ? (String) row[0] : null)
                        .costoUnitario(row[1] != null
                                ? new BigDecimal(row[1].toString()).setScale(2, RoundingMode.HALF_UP)
                                : null)
                        .fechaAdquisicion(row[2] != null ? toLocalDate(row[2]) : null)
                        .proveedor(row[3] != null ? (String) row[3] : null)
                        .build())
                .collect(Collectors.toList());

        // Calcular estadísticos
        List<BigDecimal> precios = historial.stream()
                .map(HistorialPreciosEppDTO.PuntoHistorialDTO::getCostoUnitario)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal minimo   = precios.isEmpty() ? null : precios.stream().min(BigDecimal::compareTo).orElse(null);
        BigDecimal maximo   = precios.isEmpty() ? null : precios.stream().max(BigDecimal::compareTo).orElse(null);
        BigDecimal promedio = precios.isEmpty() ? null : precios.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(precios.size()), 2, RoundingMode.HALF_UP);
        BigDecimal actual   = historial.isEmpty() ? null
                : historial.get(historial.size() - 1).getCostoUnitario();

        return HistorialPreciosEppDTO.builder()
                .eppId(eppId)
                .eppNombre(epp.getNombreEpp())
                .historial(historial)
                .precioMinimo(minimo)
                .precioMaximo(maximo)
                .precioPromedio(promedio)
                .precioActual(actual)
                .build();
    }

    /**
     * C — Valorización del inventario central.
     * Cuánto dinero hay inmovilizado en stock (cantidad × costo unitario).
     */
    @Transactional(readOnly = true)
    public ValorInventarioDTO calcularValorInventario() {
        log.info("Calculando valorización del inventario central");

        List<Object[]> raw = inventarioCentralRepository.calcularValorInventarioCentral();

        List<ValorInventarioDTO.DetalleValorDTO> detalles = raw.stream()
                .map(row -> ValorInventarioDTO.DetalleValorDTO.builder()
                        .eppId(((Number) row[0]).intValue())
                        .eppNombre((String) row[1])
                        .totalUnidades(row[2] != null ? ((Number) row[2]).intValue() : 0)
                        .costoUnitario(row[3] != null
                                ? new BigDecimal(row[3].toString()).setScale(2, RoundingMode.HALF_UP) : null)
                        .valorTotal(row[4] != null
                                ? new BigDecimal(row[4].toString()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalGeneral = detalles.stream()
                .map(ValorInventarioDTO.DetalleValorDTO::getValorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalUnidades = detalles.stream()
                .mapToInt(d -> d.getTotalUnidades() != null ? d.getTotalUnidades() : 0).sum();

        // Calcular porcentaje de cada EPP sobre el total
        if (totalGeneral.compareTo(BigDecimal.ZERO) > 0) {
            detalles.forEach(d -> {
                if (d.getValorTotal() != null) {
                    double pct = d.getValorTotal()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalGeneral, 1, RoundingMode.HALF_UP)
                            .doubleValue();
                    d.setPorcentajeValor(pct);
                }
            });
        }

        return ValorInventarioDTO.builder()
                .valorTotalCentral(totalGeneral.setScale(2, RoundingMode.HALF_UP))
                .totalEppsEnStock(detalles.size())
                .totalUnidadesCentral(totalUnidades)
                .detallePorEpp(detalles)
                .build();
    }

    /**
     * D — Trabajadores con mayor valor acumulado de EPPs recibidos.
     */
    @Transactional(readOnly = true)
    public TrabajadorMayorGastoDTO calcularTrabajadoresMayorGasto(int limite) {
        log.info("Calculando top {} trabajadores por valor de EPPs recibidos", limite);

        List<Object[]> raw = detalleEntregaEppRepository.findTrabajadoresMayorGasto();

        List<TrabajadorMayorGastoDTO.ItemTrabajadorGastoDTO> items = raw.stream()
                .limit(limite)
                .map(row -> TrabajadorMayorGastoDTO.ItemTrabajadorGastoDTO.builder()
                        .trabajadorId(((Number) row[0]).intValue())
                        .nombreCompleto((String) row[1])
                        .dni((String) row[2])
                        .areaNombre((String) row[3])
                        .totalEntregas(row[4] != null ? ((Number) row[4]).intValue() : 0)
                        .valorAcumulado(row[5] != null
                                ? new BigDecimal(row[5].toString()).setScale(2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalEntregado = items.stream()
                .map(TrabajadorMayorGastoDTO.ItemTrabajadorGastoDTO::getValorAcumulado)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular porcentaje
        if (totalEntregado.compareTo(BigDecimal.ZERO) > 0) {
            items.forEach(i -> {
                if (i.getValorAcumulado() != null) {
                    double pct = i.getValorAcumulado()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalEntregado, 1, RoundingMode.HALF_UP)
                            .doubleValue();
                    i.setPorcentaje(pct);
                }
            });
        }

        return TrabajadorMayorGastoDTO.builder()
                .ranking(items)
                .valorTotalEntregado(totalEntregado)
                .build();
    }

    /**
     * E — EPPs más comprados: frecuencia en facturas + unidades totales.
     */
    @Transactional(readOnly = true)
    public FrecuenciaComprasDTO calcularFrecuenciaCompras() {
        log.info("Calculando frecuencia de compras por EPP");

        List<FrecuenciaComprasDTO.ItemFrecuenciaDTO> items =
                detalleCompraRepository.findEppsMasComprados().stream()
                        .map(row -> FrecuenciaComprasDTO.ItemFrecuenciaDTO.builder()
                                .eppId(((Number) row[0]).intValue())
                                .eppNombre((String) row[1])
                                .vecesComprado(((Number) row[2]).longValue())
                                .totalUnidadesCompradas(((Number) row[3]).longValue())
                                .gastoTotalHistorico(row[4] != null
                                        ? new BigDecimal(row[4].toString()).setScale(2, RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO)
                                .build())
                        .collect(Collectors.toList());

        return FrecuenciaComprasDTO.builder().epps(items).build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helpers privados
    // ════════════════════════════════════════════════════════════════════════

    private BigDecimal obtenerCostoUnitario(CatalogoEpp epp) {
        return inventarioCentralRepository.findByEppOrderByCantidadActualDesc(epp)
                .stream().map(InventarioCentral::getCostoUnitario)
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    private LocalDate toLocalDate(Object val) {
        if (val instanceof LocalDate) return (LocalDate) val;
        if (val instanceof java.sql.Date) return ((java.sql.Date) val).toLocalDate();
        return null;
    }
}
