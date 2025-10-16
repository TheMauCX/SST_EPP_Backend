package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.CatalogoEpp.TipoUso;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoEppRequestDTO {

    @NotBlank(message = "El nombre del EPP es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreEpp;

    @Size(max = 50, message = "El código de identificación no puede exceder 50 caracteres")
    private String codigoIdentificacion;

    @Size(max = 1000, message = "Las especificaciones técnicas no pueden exceder 1000 caracteres")
    private String especificacionesTecnicas;

    @NotNull(message = "El tipo de uso es obligatorio")
    private TipoUso tipoUso;

    @Min(value = 1, message = "La vida útil debe ser al menos 1 mes")
    @Max(value = 120, message = "La vida útil no puede exceder 120 meses")
    private Integer vidaUtilMeses;

    @Size(max = 50, message = "El nivel de protección no puede exceder 50 caracteres")
    private String nivelProteccion;

    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    private String marca;

    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    @Pattern(regexp = "^(UNI|PAR|CAJA|SET|KIT|ROLLO|PAQUETE|METRO|LITRO)?$",
            message = "Unidad de medida inválida")
    private String unidadMedida;
}