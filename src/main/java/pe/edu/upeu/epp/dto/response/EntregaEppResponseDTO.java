package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.EntregaEpp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaEppResponseDTO {

    private Integer entregaId;
    private Integer trabajadorId;
    private String trabajadorNombre;
    private String trabajadorDni;
    private Integer jefeAreaId;
    private String jefeAreaNombre;
    private LocalDateTime fechaEntrega;
    private EntregaEpp.TipoEntrega tipoEntrega;
    private String observaciones;
    private String status;
    private List<DetalleEntregaDTO> items;
}
