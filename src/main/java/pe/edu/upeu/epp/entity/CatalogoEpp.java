package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "catalogo_epp", schema = "epp", indexes = {
        @Index(name = "idx_catalogo_epp_tipo_uso", columnList = "tipo_uso"),
        @Index(name = "idx_catalogo_epp_activo", columnList = "activo")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogoEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "epp_id")
    private Integer eppId;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre_epp", nullable = false, length = 100)
    private String nombreEpp;

    @Column(name = "codigo_identificacion", unique = true, length = 50)
    private String codigoIdentificacion;

    @Column(name = "especificaciones_tecnicas", columnDefinition = "TEXT")
    private String especificacionesTecnicas;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_uso", nullable = false, length = 20)
    private TipoUso tipoUso;

    @Column(name = "vida_util_meses")
    private Integer vidaUtilMeses;

    @Column(name = "nivel_proteccion", length = 50)
    private String nivelProteccion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "tallas", length = 100)
    private String tallas;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum TipoUso {
        CONSUMIBLE, DURADERO
    }
}