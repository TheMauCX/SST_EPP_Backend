package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.EntregaEpp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FichaTrabajadorResponseDTO {

    // Datos del trabajador
    private Integer trabajadorId;
    private String nombreCompleto;
    private String dni;
    private String cargo;
    private String areaNombre;

    // Historial de entregas
    private List<EntregaFichaDTO> entregas;
    private Integer totalEntregas;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EntregaFichaDTO {
        private Integer entregaId;
        private LocalDateTime fechaEntrega;
        private EntregaEpp.TipoEntrega tipoEntrega;
        private String observaciones;
        private String entregadoPor;   // Nombre del jefe/supervisor que entregó
        private List<ItemFichaDTO> items;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemFichaDTO {
        private String nombreEpp;
        private String talla;           // Null si EPP sin talla
        private Integer cantidad;
        private String motivo;
        private BigDecimal costoUnitario;
        private BigDecimal subtotalItem; // cantidad × costoUnitario
    }
}
