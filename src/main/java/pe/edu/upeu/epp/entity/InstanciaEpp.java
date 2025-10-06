package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "instancia_epp", schema = "epp", indexes = {
        @Index(name = "idx_instancia_epp_codigo", columnList = "codigo_serie"),
        @Index(name = "idx_instancia_epp_estado", columnList = "estado_id"),
        @Index(name = "idx_instancia_epp_trabajador", columnList = "trabajador_actual_id"),
        @Index(name = "idx_instancia_proxima_inspeccion", columnList = "fecha_proxima_inspeccion")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InstanciaEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instancia_epp_id")
    private Integer instanciaEppId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epp_id", nullable = false)
    private CatalogoEpp epp;

    @NotNull
    @Column(name = "codigo_serie", unique = true, nullable = false, length = 50)
    private String codigoSerie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoEpp estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_actual_id")
    private Area areaActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajador_actual_id")
    private Trabajador trabajadorActual;

    @NotNull
    @Column(name = "fecha_adquisicion", nullable = false)
    private java.time.LocalDate fechaAdquisicion;

    @Column(name = "fecha_vencimiento")
    private java.time.LocalDate fechaVencimiento;

    @Column(name = "lote", length = 50)
    private String lote;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_ultima_inspeccion")
    private java.time.LocalDate fechaUltimaInspeccion;

    @Column(name = "fecha_proxima_inspeccion")
    private java.time.LocalDate fechaProximaInspeccion;

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

    @Transient
    public boolean estaDisponible() {
        return estado != null && "EN_STOCK".equals(estado.getNombre());
    }
}