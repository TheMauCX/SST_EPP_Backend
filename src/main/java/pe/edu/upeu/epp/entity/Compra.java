package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de una compra/factura de EPPs.
 *
 * Sprint 4 — cambios (HU-20):
 *   - Se agregan campos: subtotal, igv, montoTotal.
 *     El campo 'montoTotal' existía antes. Ahora representa el TOTAL CON IGV.
 *     Hibernate agrega las dos columnas nuevas con ddl-auto=update (ALTER TABLE).
 *
 * Regla de negocio (Perú, IGV 18%):
 *   subtotal = montoTotal / 1.18   (redondeado a 2 decimales HALF_UP)
 *   igv      = montoTotal - subtotal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compra", schema = "epp")
@EntityListeners(AuditingEntityListener.class)
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compra_id")
    private Integer compraId;

    @Column(name = "nro_factura", nullable = false, length = 50)
    private String nroFactura;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Column(length = 150)
    private String proveedor;

    /**
     * Monto sin IGV. Calculado en CompraService.
     * HU-20
     */
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Monto del IGV (18%). Calculado en CompraService.
     * HU-20
     */
    @Column(name = "igv", precision = 10, scale = 2)
    private BigDecimal igv;

    /**
     * Total con IGV incluido. Existía en sprints anteriores.
     * Ahora es montoTotal = subtotal + igv.
     */
    @Column(name = "monto_total", precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "ruta_archivo_factura", length = 500)
    private String rutaArchivoFactura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleCompra> detalles = new ArrayList<>();

    @CreatedDate
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    public void addDetalle(DetalleCompra detalle) {
        detalles.add(detalle);
        detalle.setCompra(this);
    }
}
