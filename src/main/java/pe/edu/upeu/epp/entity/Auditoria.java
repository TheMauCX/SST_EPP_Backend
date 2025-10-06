package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria", schema = "epp", indexes = {
        @Index(name = "idx_auditoria_tabla", columnList = "tabla_afectada"),
        @Index(name = "idx_auditoria_fecha", columnList = "fecha_operacion"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditoria_id")
    private Integer auditoriaId;

    @Column(name = "tabla_afectada", nullable = false, length = 50)
    private String tablaAfectada;

    @Enumerated(EnumType.STRING)
    @Column(name = "operacion", nullable = false, length = 10)
    private TipoOperacion operacion;

    @Column(name = "registro_id")
    private Integer registroId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion;

    @Column(name = "datos_anteriores", columnDefinition = "jsonb")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "jsonb")
    private String datosNuevos;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        fechaOperacion = LocalDateTime.now();
    }

    public enum TipoOperacion {
        INSERT, UPDATE, DELETE
    }
}