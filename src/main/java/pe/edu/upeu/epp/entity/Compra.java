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
 * Registro de compra/factura.
 *
 * Sprint 4b: se agrega rutaArchivoCotizacion (campo opcional).
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

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "igv", precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(name = "monto_total", precision = 10, scale = 2)
    private BigDecimal montoTotal;

    /** URL pública de la factura en Azure (campo existente). */
    @Column(name = "ruta_archivo_factura", length = 500)
    private String rutaArchivoFactura;

    /**
     * URL pública del archivo de cotización en Azure.
     * Opcional: puede ser null si no se adjuntó cotización.
     * Sprint 4b.
     */
    @Column(name = "ruta_archivo_cotizacion", length = 500)
    private String rutaArchivoCotizacion;

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
