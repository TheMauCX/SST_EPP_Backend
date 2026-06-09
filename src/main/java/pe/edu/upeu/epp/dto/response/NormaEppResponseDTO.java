package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.NormaEpp.Categoria;
import pe.edu.upeu.epp.entity.NormaEpp.Organismo;

/**
 * DTO de respuesta para normas EPP.
 *
 * Diseñado para dos usos en la UI:
 *
 * 1. Selector (chips): se usan normaId + codigo + nombreCorto + organismo + iconoUrl
 * 2. Panel desplegable de detalle: se usa descripcion + categoria
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NormaEppResponseDTO {

    private Integer  normaId;
    private String   codigo;
    private String   nombreCorto;

    /** Texto completo para el panel desplegable de detalles. */
    private String   descripcion;

    private Organismo organismo;
    private Categoria categoria;

    /** URL del icono/emblema. Null en dev — el frontend usa el código como fallback. */
    private String   iconoUrl;

    private Boolean  activo;
}
