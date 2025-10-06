package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_area", schema = "epp",
        uniqueConstraints = @UniqueConstraint(columnNames = {"epp_id", "area_id"}),
        indexes = {
                @Index(name = "idx_inv_area_epp", columnList = "epp_id"),
                @Index(name = "idx_inv_area_area", columnList = "area_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventarioArea {

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