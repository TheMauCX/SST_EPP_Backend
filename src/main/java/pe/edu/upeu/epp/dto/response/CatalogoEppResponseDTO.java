package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp.TipoUso;

import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogoEppResponseDTO {

    private Integer eppId;
    private String  nombreEpp;
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
    private String fichaTecnicaPath;
    private String color;

    // Tallas
    private Set<CatalogoTallaResponseDTO> tallasDisponibles;

    // Normas aplicables (Sprint 5)
    /** Lista de normas para mostrar como chips con desplegable en la UI. */
    private Set<NormaEppResponseDTO> normasAplicables;

    // Umbrales de stock
    private Integer cantidadMinima;
    private Integer cantidadMaxima;

    // Auditoría
    private Boolean       activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
