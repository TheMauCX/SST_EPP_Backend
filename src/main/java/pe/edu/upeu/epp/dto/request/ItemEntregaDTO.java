package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntregaDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    // Para EPPs CONSUMIBLES: cantidad es obligatoria, instanciaEppId es null
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    // Para EPPs DURADEROS: instanciaEppId es obligatoria, cantidad es null
    private Integer instanciaEppId;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 50, message = "El motivo no puede exceder 50 caracteres")
    private String motivo; // PRIMERA_ENTREGA, REPOSICION, DESGASTE, etc.
}
