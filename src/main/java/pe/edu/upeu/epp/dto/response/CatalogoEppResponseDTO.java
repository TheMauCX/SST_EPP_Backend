package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp.TipoUso;

import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogoEppResponseDTO {

    private Integer eppId;
    private String nombreEpp;
    private TipoUso tipoUso;

    // Ficha técnica
    private String aprobacionesNormas;
    private String caracteristicas;
    private String fabricante;
    private String tiempoUsoFabricante;
    private String tiempoUsoOperacion;
    private String condicionesMantenimiento;
    private String condicionesAlmacenamiento;
    private String condicionesCambioPrematuro;
    private String fotoReferencia;

    // Sprint 4
    private String color;
    private Set<CatalogoTallaResponseDTO> tallasDisponibles;

    /** URL pública del PDF en Azure. Null si no tiene ficha técnica cargada. HU-19 */
    private String fichaTecnicaPath;

    // Auditoría
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
