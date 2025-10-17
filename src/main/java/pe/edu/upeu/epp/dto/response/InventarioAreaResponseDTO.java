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
public class InventarioAreaResponseDTO {

    private Integer inventarioAreaId;
    private Integer eppId;
    private String eppNombre;
    private String eppCodigoIdentificacion;
    private Integer areaId;
    private String areaNombre;

    private Integer estadoId;
    private String estadoNombre;
    private String estadoDescripcion;
    private Boolean estadoPermiteUso;
    private String estadoColorHex;

    private Integer cantidadActual;
    private Integer cantidadMinima;
    private Integer cantidadMaxima;
    private String ubicacion;
    private LocalDateTime ultimaActualizacion;

    private Boolean necesitaReposicion;
    private Integer porcentajeStock;
}