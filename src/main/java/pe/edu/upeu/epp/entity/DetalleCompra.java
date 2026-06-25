package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Detalle de un ítem dentro de una factura de compra.
 *
 * Fix: se agrega relación ManyToOne con CatalogoTalla para registrar
 * qué talla específica se compró. Nullable para EPPs sin talla.
 *
 * Esto completa la trazabilidad: factura → ítem (EPP + talla + cantidad + precio)
 * → registro en inventario central (EPP + lote + talla + cantidad).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_compra", schema = "epp")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_compra_id")
    private Integer detalleCompraId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epp_id", nullable = false)
    private CatalogoEpp epp;

    /**
     * Talla comprada. Nullable para EPPs sin talla.
     * Si se compra "Casco talla M × 30 unidades", aquí queda la talla M.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talla_id")
    private CatalogoTalla talla;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;
}
