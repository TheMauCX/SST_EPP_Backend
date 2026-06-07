package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Usuario del sistema (exclusivamente personal SST).
 *
 * Sprint 4b:
 *   - Se elimina la relación @OneToOne con Trabajador.
 *     Los usuarios del sistema son supervisores SST, no están vinculados
 *     a registros de trabajadores operativos.
 *   - El campo trabajador_id se deja en BD como nullable por compatibilidad
 *     histórica, pero ya no se mapea como relación JPA.
 */
@Entity
@Table(name = "usuario", schema = "epp", indexes = {
        @Index(name = "idx_usuario_nombre", columnList = "nombre_usuario"),
        @Index(name = "idx_usuario_activo", columnList = "activo")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    @NotNull
    @Size(min = 5, max = 50)
    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 50)
    private String nombreUsuario;

    @NotNull
    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Email
    @Column(name = "email", unique = true, length = 100)
    private String email;

    /** Nombre para mostrar en la UI (nombre completo del supervisor). */
    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            schema = "epp",
            joinColumns        = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    private java.util.Set<Rol> roles = new java.util.HashSet<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) activo = true;
        if (intentosFallidos == null) intentosFallidos = 0;
    }
}
