package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardGastosResponseDTO {
    private List<GastoMensualDTO> gastosMensuales;
    private BigDecimal totalAcumulado;
    private String filtroArea;   // Nombre del área o "Todas las áreas"
}
