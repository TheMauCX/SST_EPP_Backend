package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp.TipoUso;

import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogoEppUpdateDTO {

    @Size(max = 100) private String nombreEpp;
    private TipoUso tipoUso;
    @Size(max = 255) private String aprobacionesNormas;
    private String caracteristicas;
    @Size(max = 100) private String fabricante;
    @Size(max = 150) private String tiempoUsoFabricante;
    @Size(max = 150) private String tiempoUsoOperacion;
    private String condicionesMantenimiento;
    private String condicionesAlmacenamiento;
    private String condicionesCambioPrematuro;
    @Size(max = 500) private String fotoReferencia;
    @Size(max = 60)  private String color;
    private Boolean activo;
    private Set<Integer> tallaIds;

    /** Reemplaza completamente las normas asociadas. Enviar null para no modificar. */
    private Set<Integer> normaIds;

    @Min(0) private Integer cantidadMinima;
    @Min(0) private Integer cantidadMaxima;
}
