package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotacionConsumoResponseDTO {
    private List<EppRotacionDTO> mayorRotacion;
    private List<EppInmovilizadoDTO> inmovilizados;
    private String periodoDesde;    // "2025-01-01"
}
