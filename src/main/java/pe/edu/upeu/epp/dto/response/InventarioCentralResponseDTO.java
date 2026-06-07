package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioCentralResponseDTO {

    private Integer inventarioId;
    private Integer eppId;
    private String  eppNombre;
    private CatalogoEpp.TipoUso tipoUso;

    // Talla del lote
    private Integer tallaId;
    private String  tallaNombre;

    private Integer estadoId;
    private String  estadoNombre;
    private Boolean estadoPermiteUso;
    private String  estadoColorHex;

    private Integer    cantidadActual;
    private String     ubicacionBodega;
    private String     lote;
    private LocalDate  fechaAdquisicion;
    private BigDecimal costoUnitario;
    private String     proveedor;
    private LocalDate  fechaVencimiento;
    private String     observaciones;
    private LocalDateTime ultimaActualizacion;

    // Umbrales vienen del catálogo (no del lote)
    private Integer cantidadMinima;
    private Integer cantidadMaxima;

    private Boolean necesitaReposicion;
    private Integer diasParaVencer;
}
