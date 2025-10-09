package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioAreaUpdateDTO {

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadActual;

    @Min(value = 0, message = "La cantidad mínima no puede ser negativa")
    private Integer cantidadMinima;

    @Min(value = 0, message = "La cantidad máxima no puede ser negativa")
    private Integer cantidadMaxima;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String ubicacion;
}
