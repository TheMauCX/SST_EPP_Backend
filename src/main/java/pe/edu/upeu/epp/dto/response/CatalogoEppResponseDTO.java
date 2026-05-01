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
    private TipoUso tipoUso;

    // --- Campos de Ficha Técnica ---
    private String aprobacionesNormas;
    private String caracteristicas;
    private String fabricante;
    private String tiempoUsoFabricante;
    private String tiempoUsoOperacion;
    private String condicionesMantenimiento;
    private String condicionesAlmacenamiento;
    private String condicionesCambioPrematuro;
    private String fotoReferencia;

    // --- Campos de Auditoría y Estado ---
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}