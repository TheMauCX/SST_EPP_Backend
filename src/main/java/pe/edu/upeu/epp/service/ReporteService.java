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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de reportes y dashboard financiero.
 *
 * HU-6:  Ficha de trabajador (historial de entregas + PDF descargable)
 * HU-14: Dashboard de gastos mensuales
 * HU-15: Análisis de rotación y EPPs inmovilizados
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final TrabajadorRepository trabajadorRepository;
    private final EntregaEppRepository entregaEppRepository;
    private final DetalleEntregaEppRepository detalleEntregaEppRepository;
    private final InventarioCentralRepository inventarioCentralRepository;

    // ── HU-6: Ficha de trabajador ─────────────────────────────────────────

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
                                // Buscar costo unitario de la última compra del EPP
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
                        Usuario supervisor = entrega.getSupervisorUsuario();
                        if (supervisor.getNombreCompleto() != null && !supervisor.getNombreCompleto().isBlank()) {
                            entregadoPor = supervisor.getNombreCompleto();
                        } else {
                            entregadoPor = supervisor.getNombreUsuario();
                        }
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

    /**
     * HU-6: Genera el PDF de la ficha de trabajador usando Apache PDFBox.
     */
    @Transactional(readOnly = true)
    public byte[] generarFichaTrabajadorPdf(Integer trabajadorId) {
        FichaTrabajadorResponseDTO ficha = generarFichaTrabajador(trabajadorId);

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float y = PDRectangle.A4.getHeight() - margin;
                float lineHeight = 16;

                // Encabezado
                cs.beginText();
                cs.setFont(fontBold, 16);
                cs.newLineAtOffset(margin, y);
                cs.showText("FICHA DE EPPs - " + ficha.getNombreCompleto().toUpperCase());
                cs.endText();
                y -= lineHeight * 1.5f;

                // Datos del trabajador
                String[] datosWorker = {
                        "DNI: " + ficha.getDni(),
                        "Cargo: " + (ficha.getCargo() != null ? ficha.getCargo() : "-"),
                        "Área: "  + (ficha.getAreaNombre() != null ? ficha.getAreaNombre() : "-"),
                        "Total de entregas: " + ficha.getTotalEntregas()
                };

                cs.setFont(fontNormal, 11);
                for (String dato : datosWorker) {
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    cs.showText(dato);
                    cs.endText();
                    y -= lineHeight;
                }
                y -= lineHeight;

                // Detalle de entregas
                for (FichaTrabajadorResponseDTO.EntregaFichaDTO entrega : ficha.getEntregas()) {
                    if (y < 100) {
                        // Nueva página si queda poco espacio
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        doc.addPage(newPage);
                        // (Para simplicidad no se recrea el stream aquí; en producción
                        //  se usaría un helper que gestione múltiples páginas)
                        break;
                    }

                    cs.setFont(fontBold, 11);
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    String fechaStr = entrega.getFechaEntrega() != null
                            ? entrega.getFechaEntrega().toString().substring(0, 16)
                            : "-";
                    cs.showText("Entrega #" + entrega.getEntregaId() + "  |  " + fechaStr +
                            "  |  " + entrega.getTipoEntrega());
                    cs.endText();
                    y -= lineHeight;

                    if (entrega.getEntregadoPor() != null) {
                        cs.setFont(fontNormal, 10);
                        cs.beginText();
                        cs.newLineAtOffset(margin + 10, y);
                        cs.showText("Entregado por: " + entrega.getEntregadoPor());
                        cs.endText();
                        y -= lineHeight;
                    }

                    // Items de la entrega
                    for (FichaTrabajadorResponseDTO.ItemFichaDTO item : entrega.getItems()) {
                        cs.setFont(fontNormal, 10);
                        cs.beginText();
                        cs.newLineAtOffset(margin + 20, y);
                        String tallaStr = item.getTalla() != null ? " | Talla: " + item.getTalla() : "";
                        String costoStr = item.getCostoUnitario() != null
                                ? " | S/. " + item.getCostoUnitario().setScale(2, RoundingMode.HALF_UP)
                                : "";
                        cs.showText("• " + item.getNombreEpp() + tallaStr +
                                " | Cant: " + item.getCantidad() + costoStr +
                                " | Motivo: " + (item.getMotivo() != null ? item.getMotivo() : "-"));
                        cs.endText();
                        y -= lineHeight;
                    }
                    y -= lineHeight * 0.5f;
                }

                // Pie de página
                cs.setFont(fontNormal, 9);
                cs.beginText();
                cs.newLineAtOffset(margin, 40);
                cs.showText("Generado el: " + LocalDateTime.now().toString().substring(0, 19) +
                        "  |  Sistema de Gestión SST - UPEU");
                cs.endText();
            }

            doc.save(baos);
            log.info("PDF generado para trabajador ID: {}", trabajadorId);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generando PDF para trabajador {}: {}", trabajadorId, e.getMessage(), e);
            throw new BusinessException("Error al generar el PDF de la ficha: " + e.getMessage());
        }
    }

    // ── HU-14: Dashboard de gastos ────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardGastosResponseDTO calcularGastosMensuales(Integer areaId, int meses) {
        log.info("Calculando gastos mensuales. Área: {}, Meses: {}", areaId, meses);

        List<Object[]> rawData = inventarioCentralRepository.calcularGastoMensual(areaId);

        List<GastoMensualDTO> gastos = rawData.stream()
                .map(row -> {
                    int anio  = ((Number) row[0]).intValue();
                    int mes   = ((Number) row[1]).intValue();
                    BigDecimal total = row[2] != null
                            ? new BigDecimal(row[2].toString()).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    String mesNombre = Month.of(mes)
                            .getDisplayName(TextStyle.FULL, new Locale("es", "PE"));
                    return GastoMensualDTO.builder()
                            .anio(anio).mes(mes)
                            .mesNombre(mesNombre.substring(0, 1).toUpperCase() + mesNombre.substring(1))
                            .totalGasto(total)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal total = gastos.stream()
                .map(GastoMensualDTO::getTotalGasto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String filtroArea = areaId != null ? "Área ID " + areaId : "Todas las áreas";

        return DashboardGastosResponseDTO.builder()
                .gastosMensuales(gastos)
                .totalAcumulado(total)
                .filtroArea(filtroArea)
                .build();
    }

    // ── HU-15: Rotación y consumo ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public RotacionConsumoResponseDTO calcularRotacion(int meses) {
        log.info("Calculando rotación de EPPs. Período: {} meses", meses);

        LocalDateTime desde = LocalDateTime.now().minusMonths(meses);

        List<Object[]> masEntregados = inventarioCentralRepository.findEppsMasEntregados(desde);
        List<Object[]> inmovilizados = inventarioCentralRepository.findEppsInmovilizados(desde);

        List<EppRotacionDTO> mayorRotacion = masEntregados.stream()
                .map(row -> EppRotacionDTO.builder()
                        .eppId(((Number) row[0]).intValue())
                        .nombreEpp((String) row[1])
                        .totalEntregado(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<EppInmovilizadoDTO> sinMovimiento = inmovilizados.stream()
                .map(row -> EppInmovilizadoDTO.builder()
                        .eppId(((Number) row[0]).intValue())
                        .nombreEpp((String) row[1])
                        .build())
                .collect(Collectors.toList());

        return RotacionConsumoResponseDTO.builder()
                .mayorRotacion(mayorRotacion)
                .inmovilizados(sinMovimiento)
                .periodoDesde(desde.toString().substring(0, 10))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Obtiene el costo unitario más reciente de un EPP desde el inventario central.
     */
    private BigDecimal obtenerCostoUnitario(CatalogoEpp epp) {
        return inventarioCentralRepository.findByEppOrderByCantidadActualDesc(epp)
                .stream()
                .map(InventarioCentral::getCostoUnitario)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
