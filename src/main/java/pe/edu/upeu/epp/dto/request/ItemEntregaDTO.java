package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ItemEntregaDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    /**
     * ID de la talla a entregar.
     * Requerido si el EPP tiene tallas configuradas.
     * Null para EPPs sin talla.
     * HU-17
     */
    private Integer tallaId;

    @NotBlank(message = "El estadoNombre no puede estar vacío")
    private String estadoNombre;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 50)
    private String motivo;
}
