package pe.edu.upeu.epp.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Frecuencia de compra de cada tipo de EPP.
 * Diferencia de "rotación por entregas" — aquí mide cuántas veces
 * aparece el EPP en facturas de compra, no cuántas veces se entrega.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FrecuenciaComprasDTO {

    private List<ItemFrecuenciaDTO> epps;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemFrecuenciaDTO {
        private Integer    eppId;
        private String     eppNombre;
        /** Cuántas facturas distintas incluyen este EPP. */
        private Long       vecesComprado;
        /** Suma de todas las unidades compradas históricamente. */
        private Long       totalUnidadesCompradas;
        /** Gasto total histórico en este EPP (suma de subtotales de detalle). */
        private BigDecimal gastoTotalHistorico;
    }
}
