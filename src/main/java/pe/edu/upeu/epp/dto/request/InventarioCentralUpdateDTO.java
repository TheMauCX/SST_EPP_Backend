package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioCentralUpdateDTO {

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadActual;

    @Min(value = 0, message = "La cantidad mínima no puede ser negativa")
    private Integer cantidadMinima;

    @Min(value = 0, message = "La cantidad máxima no puede ser negativa")
    private Integer cantidadMaxima;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String ubicacionBodega;

    @DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
    private BigDecimal costoUnitario;

    @Size(max = 200, message = "El proveedor no puede exceder 200 caracteres")
    private String proveedor;

    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimiento;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
