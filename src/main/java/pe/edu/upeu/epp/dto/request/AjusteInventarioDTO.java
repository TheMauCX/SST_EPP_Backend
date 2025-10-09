package pe.edu.upeu.epp.dto.request;

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
public class AjusteInventarioDTO {

    @NotNull(message = "La cantidad de ajuste es obligatoria")
    private Integer cantidadAjuste; // Puede ser positiva (ingreso) o negativa (salida)

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    @NotNull(message = "El ID del usuario responsable es obligatorio")
    private Integer usuarioResponsableId;
}
