package pe.edu.upeu.epp.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Precio unitario actual de cada EPP (lote más reciente).
 * Usado para el gráfico de barras que compara precios de todos los EPPs.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PrecioEppDTO {
    private Integer eppId;
    private String  eppNombre;
    private BigDecimal costoUnitario;
    private LocalDate  fechaAdquisicion;
    private String     proveedor;
}
