package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp.TipoUso;

import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogoEppRequestDTO {

    @NotBlank(message = "El nombre del EPP es obligatorio")
    @Size(max = 100)
    private String nombreEpp;

    @NotNull(message = "El tipo de uso es obligatorio")
    private TipoUso tipoUso;

    // Ficha técnica
    @Size(max = 255) private String aprobacionesNormas;
    private String caracteristicas;
    @Size(max = 100) private String fabricante;
    @Size(max = 150) private String tiempoUsoFabricante;
    @Size(max = 150) private String tiempoUsoOperacion;
    private String condicionesMantenimiento;
    private String condicionesAlmacenamiento;
    private String condicionesCambioPrematuro;
    @Size(max = 500) private String fotoReferencia;
    @Size(max = 60)  private String color;

    // Tallas
    private Set<Integer> tallaIds;

    // Umbrales de stock (sprint 4b — movidos desde inventarios)
    @Min(value = 0, message = "La cantidad mínima no puede ser negativa")
    private Integer cantidadMinima;

    @Min(value = 0, message = "La cantidad máxima no puede ser negativa")
    private Integer cantidadMaxima;
}
