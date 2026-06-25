package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Detalle de una entrega de EPP a un trabajador.
 *
 * Sprint 4 — cambios (HU-17):
 *   - Se agrega relación ManyToOne con CatalogoTalla para registrar
 *     qué talla específica se entregó al trabajador.
 *     Nullable para EPPs sin talla.
 */
@Entity
@Table(name = "detalle_entrega_epp", schema = "epp", indexes = {
        @Index(name = "idx_detalle_entrega", columnList = "entrega_id"),
        @Index(name = "idx_detalle_epp",     columnList = "epp_id"),
        @Index(name = "idx_detalle_talla",   columnList = "talla_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DetalleEntregaEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_id")
    private Integer detalleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_id", nullable = false)
    private EntregaEpp entrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epp_id", nullable = false)
    private CatalogoEpp epp;

    /**
     * Talla entregada. Nullable para EPPs sin talla.
     * HU-17
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talla_id")
    private CatalogoTalla talla;

    @Min(1)
    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "motivo", length = 50)
    private String motivo;
}
