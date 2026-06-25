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
    @Size(max = 255) private String aprobacionesNormas;  // legacy, opcional
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

    // Normas (Sprint 5 — HU-18)
    /** IDs de las normas a asociar. Se muestran como chips en la UI. */
    private Set<Integer> normaIds;

    // Umbrales de stock
    @Min(0) private Integer cantidadMinima;
    @Min(0) private Integer cantidadMaxima;
}
