package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DetalleCompraRequestDTO {

    @NotNull(message = "El ID del EPP es obligatorio")
    private Integer eppId;

    /**
     * Talla del EPP comprado.
     * Obligatorio si el EPP maneja tallas (cascos, guantes, botas, ropa).
     * Dejar null para EPPs sin talla (mascarillas caja x50, tapones, etc.).
     * El ID se obtiene desde GET /api/v1/tallas
     */
    private Integer tallaId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;

    @Min(value = 0, message = "La cantidad mínima no puede ser negativa")
    private Integer cantidadMinima;

    @Min(value = 0, message = "La cantidad máxima no puede ser negativa")
    private Integer cantidadMaxima;

    private LocalDate fechaVencimiento;

    private String observaciones;
}
