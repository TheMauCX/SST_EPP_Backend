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
public class InventarioAreaRequestDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    @NotNull(message = "El ID del área es obligatorio")
    private Integer areaId;

    @NotNull(message = "El ID del estado es obligatorio")
    private Integer estadoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadActual;

    @NotNull(message = "La cantidad mínima es obligatoria")
    @Min(value = 0, message = "La cantidad mínima no puede ser negativa")
    private Integer cantidadMinima;

    @Min(value = 0, message = "La cantidad máxima no puede ser negativa")
    private Integer cantidadMaxima;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String ubicacion;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}