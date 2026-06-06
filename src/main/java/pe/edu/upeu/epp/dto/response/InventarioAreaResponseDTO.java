package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.CatalogoEpp;

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
    private CatalogoEpp.TipoUso tipoUso;

    // Talla de este stock (null si el EPP no maneja tallas)
    private Integer tallaId;
    private String tallaNombre;

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
