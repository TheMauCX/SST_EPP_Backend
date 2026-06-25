package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioAreaRequestDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    @NotNull(message = "El ID del área es obligatorio")
    private Integer areaId;

    @NotNull(message = "El ID del estado es obligatorio")
    private Integer estadoId;

    private Integer tallaId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0)
    private Integer cantidadActual;

    @Size(max = 100)
    private String ubicacion;
}
