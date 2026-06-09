package pe.edu.upeu.epp.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Historial de precios unitarios de un EPP a lo largo del tiempo.
 * Cada entrada representa una compra distinta (lote) con su precio.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HistorialPreciosEppDTO {

    private Integer eppId;
    private String  eppNombre;

    private List<PuntoHistorialDTO> historial;

    /** Precio mínimo histórico. */
    private BigDecimal precioMinimo;
    /** Precio máximo histórico. */
    private BigDecimal precioMaximo;
    /** Precio promedio histórico. */
    private BigDecimal precioPromedio;
    /** Precio del lote más reciente. */
    private BigDecimal precioActual;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PuntoHistorialDTO {
        private String     lote;
        private BigDecimal costoUnitario;
        private LocalDate  fechaAdquisicion;
        private String     proveedor;
    }
}
