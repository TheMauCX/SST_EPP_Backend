package pe.edu.upeu.epp.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Valorización total del inventario: cuánto dinero hay inmovilizado en stock.
 *
 * Cálculo: SUM(cantidad_actual × costo_unitario) por EPP y en total.
 * El inventario de área no tiene costo unitario propio, así que su
 * valorización usa el costo del lote más reciente en inventario central.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ValorInventarioDTO {

    /** Valor total del inventario central. */
    private BigDecimal valorTotalCentral;
    /** Número de EPPs distintos con stock. */
    private Integer    totalEppsEnStock;
    /** Total de unidades en central. */
    private Integer    totalUnidadesCentral;

    /** Desglose por tipo de EPP (ordenado de mayor a menor valor). */
    private List<DetalleValorDTO> detallePorEpp;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleValorDTO {
        private Integer    eppId;
        private String     eppNombre;
        private Integer    totalUnidades;
        private BigDecimal costoUnitario;
        private BigDecimal valorTotal;
        /** Porcentaje sobre el valor total del inventario. */
        private Double     porcentajeValor;
    }
}
