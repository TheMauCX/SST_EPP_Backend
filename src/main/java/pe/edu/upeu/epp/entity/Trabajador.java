package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trabajador", schema = "epp", indexes = {
        @Index(name = "idx_trabajador_dni", columnList = "dni"),
        @Index(name = "idx_trabajador_qr", columnList = "codigo_qr_photocheck"),
        @Index(name = "idx_trabajador_area", columnList = "area_id"),
        @Index(name = "idx_trabajador_estado", columnList = "estado")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trabajador_id")
    private Integer trabajadorId;

    @NotNull
    @Pattern(regexp = "\\d{8,10}")
    @Column(name = "dni", unique = true, nullable = false, length = 10)
    private String dni;

    @NotNull
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotNull
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "codigo_qr_photocheck", unique = true, length = 50)
    private String codigoQrPhotocheck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(name = "puesto", length = 100)
    private String puesto;

    @Column(name = "fecha_ingreso")
    private java.time.LocalDate fechaIngreso;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Email
    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoTrabajador estado = EstadoTrabajador.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoTrabajador {
        ACTIVO, INACTIVO, SUSPENDIDO
    }
}