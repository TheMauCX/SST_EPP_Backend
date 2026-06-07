package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.EntregaEpp;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EntregaDetalleResponseDTO {

    private Integer entregaId;

    private Integer trabajadorId;
    private String  trabajadorNombre;
    private String  trabajadorDni;
    private String  trabajadorArea;
    private String  trabajadorPuesto;

    private Integer supervisorId;
    private String  supervisorNombre;

    private LocalDateTime fechaEntrega;
    private EntregaEpp.TipoEntrega tipoEntrega;
    private String observaciones;
    private String status;

    private List<DetalleEntregaDTO> items;
    private Integer totalItems;
}
