package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.CatalogoEpp.TipoUso;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoEppResponseDTO {

    private Integer eppId;
    private String nombreEpp;
    private String codigoIdentificacion;
    private String especificacionesTecnicas;
    private TipoUso tipoUso;
    private Integer vidaUtilMeses;
    private String nivelProteccion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String marca;
    private String unidadMedida;
}