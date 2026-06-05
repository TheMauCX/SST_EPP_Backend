package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Inventario por Área.
 *
 * Sprint 4 — cambios (HU-17):
 *   - Se agrega relación ManyToOne con CatalogoTalla (campo talla_id, nullable)
 *   - La clave única cambia a (epp_id, area_id, estado_id, talla_id)
 */
@Entity
@Table(name = "inventario_area", schema = "epp",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventario_area_epp_area_estado_talla",
                columnNames = {"epp_id", "area_id", "estado_id", "talla_id"}
        ),
        indexes = {
                @Index(name = "idx_inv_area_epp",    columnList = "epp_id"),
                @Index(name = "idx_inv_area_area",   columnList = "area_id"),
                @Index(name = "idx_inv_area_estado", columnList = "estado_id"),
                @Index(name = "idx_inv_area_talla",  columnList = "talla_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventarioArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventario_area_id")
    private Integer inventarioAreaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epp_id", nullable = false)
    private CatalogoEpp epp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoEpp estado;

    /**
     * Talla de este registro de stock. Nullable para EPPs sin talla.
     * HU-17
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talla_id")
    private CatalogoTalla talla;

    @Min(0)
    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual = 0;

    @Min(0)
    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima = 0;

    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }

    @Transient
    public boolean necesitaReposicion() {
        return cantidadActual <= cantidadMinima;
    }

    @Transient
    public boolean esStockUtilizable() {
        return estado != null && Boolean.TRUE.equals(estado.getPermiteUso());
    }

    @Transient
    public Integer calcularPorcentajeStock() {
        if (cantidadMaxima == null || cantidadMaxima == 0) return null;
        return (cantidadActual * 100) / cantidadMaxima;
    }
}
