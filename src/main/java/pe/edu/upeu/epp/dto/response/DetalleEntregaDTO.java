package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.CatalogoEpp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleEntregaDTO {

    private Integer detalleId;
    private Integer eppId;
    private String eppNombre;
    private CatalogoEpp.TipoUso tipoUso;
    private Integer cantidad; // Para CONSUMIBLES
    private Integer instanciaEppId; // Para DURADEROS
    private String codigoSerie; // Para DURADEROS
    private String motivo;

    private String eppMarca;           // ← NUEVO
    private String eppUnidadMedida;    // ← NUEVO
}
