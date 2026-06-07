package pe.edu.upeu.epp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.epp.entity.EntregaEpp.TipoEntrega;

import java.util.List;

/**
 * Sprint 4b: se elimina jefeAreaId.
 * El supervisor que registra la entrega se obtiene del token JWT.
 * El área de descuento se deriva del área del trabajador receptor.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EntregaEppRequestDTO {

    @NotNull(message = "El ID del trabajador es obligatorio")
    private Integer trabajadorId;

    @NotNull(message = "El tipo de entrega es obligatorio")
    private TipoEntrega tipoEntrega;

    @Size(max = 500)
    private String observaciones;

    @NotEmpty(message = "Debe incluir al menos un item")
    @Valid
    private List<ItemEntregaDTO> items;
}
