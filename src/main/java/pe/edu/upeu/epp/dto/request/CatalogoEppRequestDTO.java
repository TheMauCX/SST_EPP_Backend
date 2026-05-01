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

    @NotNull(message = "El tipo de uso es obligatorio")
    private TipoUso tipoUso;

    // --- Campos de Ficha Técnica ---

    @Size(max = 255, message = "Las aprobaciones o normas no pueden exceder 255 caracteres")
    private String aprobacionesNormas;

    private String caracteristicas;

    @Size(max = 100, message = "El fabricante no puede exceder 100 caracteres")
    private String fabricante;

    @Size(max = 150, message = "El tiempo de uso por fabricante no puede exceder 150 caracteres")
    private String tiempoUsoFabricante;

    @Size(max = 150, message = "El tiempo de uso por operación no puede exceder 150 caracteres")
    private String tiempoUsoOperacion;

    private String condicionesMantenimiento;

    private String condicionesAlmacenamiento;

    private String condicionesCambioPrematuro;

    @Size(max = 500, message = "La URL de la foto de referencia no puede exceder 500 caracteres")
    private String fotoReferencia;
}