package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajador_id", unique = true)
    private Trabajador trabajador;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            schema = "epp",
            joinColumns = @JoinColumn(name = "usuario_id"),
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