package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioCentralResponseDTO {

    private Integer inventarioCentralId;
    private Integer eppId;
    private String eppNombre;
    private String eppCodigoIdentificacion;

    private Integer estadoId;
    private String estadoNombre;
    private String estadoDescripcion;
    private Boolean estadoPermiteUso;
    private String estadoColorHex;

    private Integer cantidadActual;
    private Integer cantidadMinima;
    private Integer cantidadMaxima;
    private String ubicacionBodega;
    private String lote;
    private LocalDate fechaAdquisicion;
    private BigDecimal costoUnitario;
    private String proveedor;
    private LocalDate fechaVencimiento;
    private String observaciones;
    private LocalDateTime ultimaActualizacion;

    private Boolean necesitaReposicion;
    private Integer diasParaVencer;
}