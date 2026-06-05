package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransferenciaStockDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    @NotNull(message = "El ID del área destino es obligatorio")
    private Integer areaId;

    @NotNull(message = "La cantidad a transferir es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    /**
     * ID de la talla a transferir.
     * Obligatorio si el EPP maneja tallas.
     * Null para EPPs sin talla (mascarillas, tapones, etc.).
     * HU-17
     */
    private Integer tallaId;

    @Size(max = 500)
    private String motivo;
}
