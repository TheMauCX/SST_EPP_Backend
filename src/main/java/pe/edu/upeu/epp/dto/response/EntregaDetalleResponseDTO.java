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
public class EntregaDetalleResponseDTO {

    private Integer entregaId;

    // Datos del trabajador
    private Integer trabajadorId;
    private String trabajadorNombre;
    private String trabajadorDni;
    private String trabajadorArea;
    private String trabajadorPuesto;

    // Datos del jefe
    private Integer jefeAreaId;
    private String jefeAreaNombre;

    // Datos de la entrega
    private LocalDateTime fechaEntrega;
    private EntregaEpp.TipoEntrega tipoEntrega;
    private String observaciones;
    private String status;

    // Items entregados
    private List<DetalleEntregaDTO> items;
    private Integer totalItems;
}
