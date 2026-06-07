package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inventario Central.
 *
 * Sprint 4b: se eliminan cantidadMinima y cantidadMaxima.
 * Los umbrales de alerta ahora viven en CatalogoEpp y aplican
 * globalmente para ese tipo de EPP, independientemente del lote.
 */
@Entity
@Table(name = "inventario_central", schema = "epp",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventario_central_epp_lote_estado_talla",
                columnNames = {"epp_id", "lote", "estado_id", "talla_id"}
        ),
        indexes = {
                @Index(name = "idx_inv_central_epp",         columnList = "epp_id"),
                @Index(name = "idx_inv_central_estado",      columnList = "estado_id"),
                @Index(name = "idx_inv_central_talla",       columnList = "talla_id"),
                @Index(name = "idx_inv_central_vencimiento", columnList = "fecha_vencimiento")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventarioCentral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventario_central_id")
    private Integer inventarioCentralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epp_id", nullable = false)
    private CatalogoEpp epp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoEpp estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talla_id")
    private CatalogoTalla talla;

    @Min(0)
    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual = 0;

    @Column(name = "ubicacion_bodega", length = 100)
    private String ubicacionBodega;

    @Column(name = "lote", length = 50)
    private String lote;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "proveedor", length = 200)
    private String proveedor;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }

    // ── Helpers transient ────────────────────────────────────────────────────

    /**
     * Stock disponible: solo cuenta si el estado permite uso.
     */
    @Transient
    public Integer getCantidadDisponible() {
        if (this.estado != null && Boolean.TRUE.equals(this.estado.getPermiteUso())) {
            return this.cantidadActual;
        }
        return 0;
    }

    /**
     * Necesita reposición: compara con el umbral definido en el catálogo del EPP.
     */
    @Transient
    public boolean necesitaReposicion() {
        if (this.epp == null || this.epp.getCantidadMinima() == null) return false;
        return cantidadActual <= this.epp.getCantidadMinima();
    }

    @Transient
    public boolean esStockUtilizable() {
        return estado != null && Boolean.TRUE.equals(estado.getPermiteUso());
    }
}
