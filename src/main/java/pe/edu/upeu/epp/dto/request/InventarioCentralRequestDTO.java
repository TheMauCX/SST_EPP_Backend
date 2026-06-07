package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioCentralRequestDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    @NotNull(message = "El ID del estado es obligatorio")
    private Integer estadoId;

    /** Talla del lote. Null para EPPs sin talla. */
    private Integer tallaId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadActual;

    @Size(max = 100)
    private String ubicacionBodega;

    @NotBlank(message = "El lote es obligatorio")
    @Size(max = 50)
    private String lote;

    @NotNull(message = "La fecha de adquisición es obligatoria")
    @PastOrPresent
    private LocalDate fechaAdquisicion;

    @DecimalMin(value = "0.01")
    private BigDecimal costoUnitario;

    @Size(max = 200)
    private String proveedor;

    @Future
    private LocalDate fechaVencimiento;

    @Size(max = 500)
    private String observaciones;
}
