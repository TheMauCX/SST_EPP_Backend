package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaResponseDTO {

    private Integer areaId;
    private String nombreArea;
    private String codigoArea;
    private String descripcion;
    private String ubicacion;
    private Integer responsableId;
    private String responsableNombre;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}