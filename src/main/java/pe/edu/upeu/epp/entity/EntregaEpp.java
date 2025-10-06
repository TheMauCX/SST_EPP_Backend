package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entrega_epp", schema = "epp", indexes = {
        @Index(name = "idx_entrega_trabajador", columnList = "trabajador_id"),
        @Index(name = "idx_entrega_fecha", columnList = "fecha_entrega"),
        @Index(name = "idx_entrega_tipo", columnList = "tipo_entrega")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jefe_area_id", nullable = false)
    private Trabajador jefeArea;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false, length = 30)
    private TipoEntrega tipoEntrega;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "firma_digital", columnDefinition = "TEXT")
    private String firmaDigital;

    @Column(name = "status", length = 20)
    private String status = "COMPLETADA";

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<DetalleEntregaEpp> detalles = new java.util.ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaEntrega == null) {
            fechaEntrega = LocalDateTime.now();
        }
    }

    public void agregarDetalle(DetalleEntregaEpp detalle) {
        detalles.add(detalle);
        detalle.setEntrega(this);
    }

    public enum TipoEntrega {
        PRIMERA_ENTREGA, REPOSICION, EMERGENCIA
    }
}