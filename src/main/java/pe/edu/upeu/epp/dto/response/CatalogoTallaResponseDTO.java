package pe.edu.upeu.epp.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogoTallaResponseDTO {
    private Integer tallaId;
    private String nombre;
    private String descripcion;
    private Integer ordenVisualizacion;
}
