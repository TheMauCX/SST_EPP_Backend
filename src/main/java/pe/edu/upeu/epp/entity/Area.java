package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "area", schema = "epp", indexes = {
        @Index(name = "idx_area_activo", columnList = "activo")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Integer areaId;

    @NotNull
    @Column(name = "nombre_area", nullable = false, unique = true, length = 100)
    private String nombreArea;

    @Column(name = "codigo_area", unique = true, length = 20)
    private String codigoArea;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "responsable_id")
    private Integer responsableId;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}