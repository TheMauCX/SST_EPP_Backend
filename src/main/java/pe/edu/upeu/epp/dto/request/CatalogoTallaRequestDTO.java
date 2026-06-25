package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogoTallaRequestDTO {

    @NotBlank(message = "El nombre de la talla es obligatorio")
    @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
    private String nombre;

    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    private String descripcion;

    private Integer ordenVisualizacion;
}
