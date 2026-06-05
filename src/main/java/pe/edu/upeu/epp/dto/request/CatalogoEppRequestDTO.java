package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp.TipoUso;

import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogoEppRequestDTO {

    @NotBlank(message = "El nombre del EPP es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreEpp;

    @NotNull(message = "El tipo de uso es obligatorio")
    private TipoUso tipoUso;

    // ── Ficha técnica ────────────────────────────────────────────────────

    @Size(max = 255)
    private String aprobacionesNormas;
    private String caracteristicas;
    @Size(max = 100) private String fabricante;
    @Size(max = 150) private String tiempoUsoFabricante;
    @Size(max = 150) private String tiempoUsoOperacion;
    private String condicionesMantenimiento;
    private String condicionesAlmacenamiento;
    private String condicionesCambioPrematuro;

    @Size(max = 500)
    private String fotoReferencia;

    // ── Sprint 4: nuevos campos ──────────────────────────────────────────

    /** Color del EPP. HU-17 */
    @Size(max = 60, message = "El color no puede exceder 60 caracteres")
    private String color;

    /**
     * IDs de las tallas disponibles para este EPP.
     * Si viene vacío o null, el EPP no maneja tallas.
     * HU-17
     */
    private Set<Integer> tallaIds;
}
