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
@Table(name = "catalogo_epp")
@EntityListeners(AuditingEntityListener.class)
@Data // Incluye @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor // Constructor vacío
@AllArgsConstructor // Constructor con todos los campos
@RequiredArgsConstructor // Constructor con campos @NonNull
public class CatalogoEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "epp_id")
    private Integer eppId;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre_epp", nullable = false, length = 100)
    @NonNull
    private String nombreEpp;

    @Column(name = "codigo_identificacion", unique = true, length = 50)
    @NonNull
    private String codigoIdentificacion;

    @Column(name = "especificaciones_tecnicas", columnDefinition = "TEXT")
    private String especificacionesTecnicas;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_uso", length = 20)
    @NonNull
    private TipoUso tipoUso;

    @Column(name = "vida_util_meses")
    private Integer vidaUtilMeses;

    @Column(name = "nivel_proteccion", length = 50)
    private String nivelProteccion;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "activo")
    private Boolean activo = true;
}


enum TipoUso {
    CONSUMIBLE, DURADERO
}