package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioCentralUpdateDTO {

    private Integer estadoId;

    @Size(max = 100)
    private String ubicacionBodega;

    @Size(max = 200)
    private String proveedor;

    @Future
    private LocalDate fechaVencimiento;

    @Size(max = 500)
    private String observaciones;

    @NotBlank(message = "El lote es obligatorio")
    @Size(max = 50)
    private String lote;
}
