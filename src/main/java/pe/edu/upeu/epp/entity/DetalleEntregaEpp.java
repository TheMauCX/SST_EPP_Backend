package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "detalle_entrega_epp", schema = "epp", indexes = {
        @Index(name = "idx_detalle_entrega", columnList = "entrega_id"),
        @Index(name = "idx_detalle_epp", columnList = "epp_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_epp_id")
    private InstanciaEpp instanciaEpp;

    @Min(1)
    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "motivo", length = 50)
    private String motivo;
}

