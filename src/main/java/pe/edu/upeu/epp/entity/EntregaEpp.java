package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de entrega de EPP a un trabajador.
 *
 * Sprint 4b:
 *   - Se renombra jefeArea → supervisorUsuario.
 *     La columna en BD pasa de jefe_area_id a supervisor_usuario_id (script SQL).
 *   - El supervisor puede entregar a trabajadores de cualquier área.
 *     El área de descuento se deriva del trabajador receptor (Opción B acordada).
 */
@Entity
@Table(name = "entrega_epp", schema = "epp", indexes = {
        @Index(name = "idx_entrega_trabajador",  columnList = "trabajador_id"),
        @Index(name = "idx_entrega_fecha",       columnList = "fecha_entrega"),
        @Index(name = "idx_entrega_tipo",        columnList = "tipo_entrega"),
        @Index(name = "idx_entrega_supervisor",  columnList = "supervisor_usuario_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntregaEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entrega_id")
    private Integer entregaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Trabajador trabajador;

    /**
     * Usuario supervisor SST que registra la entrega.
     * Columna: supervisor_usuario_id (renombrada desde jefe_area_id en sprint 4b).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_usuario_id", nullable = false)
    private Usuario supervisorUsuario;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false, length = 30)
    private TipoEntrega tipoEntrega;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "status", length = 20)
    private String status = "COMPLETADA";

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<DetalleEntregaEpp> detalles = new java.util.ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaEntrega == null) fechaEntrega = LocalDateTime.now();
    }

    public void agregarDetalle(DetalleEntregaEpp detalle) {
        detalles.add(detalle);
        detalle.setEntrega(this);
    }

    public enum TipoEntrega {
        PRIMERA_ENTREGA, REPOSICION, EMERGENCIA
    }
}
