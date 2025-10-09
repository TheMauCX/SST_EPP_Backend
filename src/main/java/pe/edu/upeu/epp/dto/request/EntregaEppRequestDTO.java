// ============================================
// EntregaEppRequestDTO.java
// ============================================
package pe.edu.upeu.epp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.EntregaEpp.TipoEntrega;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaEppRequestDTO {

    @NotNull(message = "El ID del trabajador es obligatorio")
    private Integer trabajadorId;

    @NotNull(message = "El ID del jefe de área es obligatorio")
    private Integer jefeAreaId;

    @NotNull(message = "El tipo de entrega es obligatorio")
    private TipoEntrega tipoEntrega;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @Size(max = 1000, message = "La firma digital no puede exceder 1000 caracteres")
    private String firmaDigital; // Base64 de la firma

    @NotEmpty(message = "Debe incluir al menos un item")
    @Valid
    private List<ItemEntregaDTO> items;
}