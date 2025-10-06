package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_reposicion", schema = "epp", indexes = {
        @Index(name = "idx_solicitud_estado", columnList = "estado_solicitud"),
        @Index(name = "idx_solicitud_prioridad", columnList = "prioridad"),
        @Index(name = "idx_solicitud_area", columnList = "area_id"),
        @Index(name = "idx_solicitud_fecha", columnList = "fecha_solicitud")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudReposicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solicitud_id")
    private Integer solicitudId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epp_id", nullable = false)
    private CatalogoEpp epp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Trabajador solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Trabajador supervisor;

    @Min(1)
    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Min(1)
    @Column(name = "cantidad_aprobada")
    private Integer cantidadAprobada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false, length = 20)
    private EstadoSolicitud estadoSolicitud = EstadoSolicitud.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20)
    private Prioridad prioridad = Prioridad.MEDIA;

    @NotNull
    @Column(name = "justificacion", nullable = false, columnDefinition = "TEXT")
    private String justificacion;

    @Column(name = "comentarios_supervisor", columnDefinition = "TEXT")
    private String comentariosSupervisor;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_rechazo")
    private LocalDateTime fechaRechazo;

    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
        if (estadoSolicitud == null) estadoSolicitud = EstadoSolicitud.PENDIENTE;
        if (prioridad == null) prioridad = Prioridad.MEDIA;
    }

    public enum EstadoSolicitud {
        PENDIENTE, APROBADA, RECHAZADA, CANCELADA
    }

    public enum Prioridad {
        BAJA, MEDIA, ALTA, URGENTE
    }
}