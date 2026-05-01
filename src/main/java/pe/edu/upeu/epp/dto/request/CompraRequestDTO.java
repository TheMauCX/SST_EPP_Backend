package pe.edu.upeu.epp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CompraRequestDTO {

    @NotBlank(message = "El número de factura es obligatorio")
    private String nroFactura;

    @NotNull(message = "La fecha de compra es obligatoria")
    private LocalDate fechaCompra;

    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;

    // Arreglo de ítems comprados
    @NotEmpty(message = "Debe incluir al menos un EPP en la compra")
    @Valid
    private List<DetalleCompraRequestDTO> items;
}