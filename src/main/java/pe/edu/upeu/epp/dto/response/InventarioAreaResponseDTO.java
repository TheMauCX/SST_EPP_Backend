package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioAreaResponseDTO {

    private Integer inventarioAreaId;
    private Integer eppId;
    private String  eppNombre;
    private Integer areaId;
    private String  areaNombre;
    private CatalogoEpp.TipoUso tipoUso;

    // Talla
    private Integer tallaId;
    private String  tallaNombre;

    private Integer estadoId;
    private String  estadoNombre;
    private Boolean estadoPermiteUso;
    private String  estadoColorHex;

    private Integer cantidadActual;
    private String  ubicacion;
    private LocalDateTime ultimaActualizacion;

    // Umbrales del catálogo
    private Integer cantidadMinima;
    private Integer cantidadMaxima;

    private Boolean necesitaReposicion;
    private Integer porcentajeStock;
}
