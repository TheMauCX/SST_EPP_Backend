package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.Min;
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
public class TransferenciaStockDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    @NotNull(message = "El ID del área destino es obligatorio")
    private Integer areaId;

    @NotNull(message = "La cantidad a transferir es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;
}