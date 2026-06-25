package pe.edu.upeu.epp.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Ranking de trabajadores por el valor monetario total de EPPs que han recibido.
 * Útil para identificar quién consume más presupuesto de EPP.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TrabajadorMayorGastoDTO {

    private List<ItemTrabajadorGastoDTO> ranking;
    /** Valor total entregado a todos los trabajadores en el período. */
    private BigDecimal valorTotalEntregado;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemTrabajadorGastoDTO {
        private Integer    trabajadorId;
        private String     nombreCompleto;
        private String     dni;
        private String     areaNombre;
        private Integer    totalEntregas;
        /** Suma de (cantidad entregada × costo unitario). */
        private BigDecimal valorAcumulado;
        /** Porcentaje sobre el total entregado. */
        private Double     porcentaje;
    }
}
