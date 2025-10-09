package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaRequestDTO {

    @NotBlank(message = "El nombre del área es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreArea;

    @Size(max = 20, message = "El código del área no puede exceder 20 caracteres")
    private String codigoArea;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 200, message = "La ubicación no puede exceder 200 caracteres")
    private String ubicacion;

    private Integer responsableId;
}