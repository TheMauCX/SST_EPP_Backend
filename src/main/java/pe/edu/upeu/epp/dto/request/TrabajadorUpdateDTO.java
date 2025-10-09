package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.Trabajador;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorUpdateDTO {

    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres")
    private String nombres;

    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    private String apellidos;

    private Integer areaId;

    @Size(max = 100, message = "El puesto no puede exceder 100 caracteres")
    private String puesto;

    @Past(message = "La fecha de ingreso debe ser en el pasado")
    private LocalDate fechaIngreso;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Formato de teléfono inválido")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "El formato del email es inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    private Trabajador.EstadoTrabajador estado;
}
