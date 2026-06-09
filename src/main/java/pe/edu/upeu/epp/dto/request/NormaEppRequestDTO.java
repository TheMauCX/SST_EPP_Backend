// ─── NormaEppRequestDTO.java ───────────────────────────────────────────────
package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.edu.upeu.epp.entity.NormaEpp.Categoria;
import pe.edu.upeu.epp.entity.NormaEpp.Organismo;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NormaEppRequestDTO {

    @NotBlank(message = "El código de la norma es obligatorio")
    @Size(max = 30)
    private String codigo;

    @NotBlank(message = "El nombre corto es obligatorio")
    @Size(max = 80)
    private String nombreCorto;

    private String descripcion;

    @NotNull(message = "El organismo es obligatorio")
    private Organismo organismo;

    @NotNull(message = "La categoría es obligatoria")
    private Categoria categoria;

    @Size(max = 500)
    private String iconoUrl;
}
