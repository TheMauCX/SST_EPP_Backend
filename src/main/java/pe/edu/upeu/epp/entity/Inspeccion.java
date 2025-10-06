package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspeccion", schema = "epp", indexes = {
        @Index(name = "idx_inspeccion_instancia", columnList = "instancia_epp_id"),
        @Index(name = "idx_inspeccion_fecha", columnList = "fecha_inspeccion"),
        @Index(name = "idx_inspeccion_resultado", columnList = "resultado"),
        @Index(name = "idx_inspeccion_proxima", columnList = "fecha_proxima_inspeccion")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inspeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspeccion_id")
    private Integer inspeccionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_epp_id", nullable = false)
    private InstanciaEpp instanciaEpp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private Trabajador inspector;

    @Column(name = "fecha_inspeccion", nullable = false)
    private LocalDateTime fechaInspeccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 30)
    private ResultadoInspeccion resultado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "url_foto", length = 500)
    private String urlFoto;

    @Column(name = "accion_correctiva", columnDefinition = "TEXT")
    private String accionCorrectiva;

    @Column(name = "fecha_proxima_inspeccion")
    private java.time.LocalDate fechaProximaInspeccion;

    @PrePersist
    protected void onCreate() {
        if (fechaInspeccion == null) {
            fechaInspeccion = LocalDateTime.now();
        }
    }

    public enum ResultadoInspeccion {
        APTO, NO_APTO, REQUIERE_MANTENIMIENTO
    }
}