package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoMensualDTO {
    private Integer anio;
    private Integer mes;
    private String mesNombre;   // "Enero", "Febrero"…
    private BigDecimal totalGasto;
}
