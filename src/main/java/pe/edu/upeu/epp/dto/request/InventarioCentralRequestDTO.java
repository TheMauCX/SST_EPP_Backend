package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
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
public class InventarioCentralRequestDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

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
    private String ubicacionBodega;

    @NotBlank(message = "El lote es obligatorio")
    @Size(max = 50, message = "El lote no puede exceder 50 caracteres")
    private String lote;

    @NotNull(message = "La fecha de adquisición es obligatoria")
    @PastOrPresent(message = "La fecha de adquisición no puede ser futura")
    private LocalDate fechaAdquisicion;

    @DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
    private BigDecimal costoUnitario;

    @Size(max = 200, message = "El proveedor no puede exceder 200 caracteres")
    private String proveedor;

    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimiento;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}