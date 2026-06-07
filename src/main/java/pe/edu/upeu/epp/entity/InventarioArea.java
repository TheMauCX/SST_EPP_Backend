package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Inventario por Área.
 *
 * Sprint 4b: se eliminan cantidadMinima y cantidadMaxima.
 * Los umbrales de alerta viven en CatalogoEpp.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talla_id")
    private CatalogoTalla talla;

    @Min(0)
    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual = 0;

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
        if (this.epp == null || this.epp.getCantidadMinima() == null) return false;
        return cantidadActual <= this.epp.getCantidadMinima();
    }

    @Transient
    public boolean esStockUtilizable() {
        return estado != null && Boolean.TRUE.equals(estado.getPermiteUso());
    }

    @Transient
    public Integer calcularPorcentajeStock() {
        Integer max = this.epp != null ? this.epp.getCantidadMaxima() : null;
        if (max == null || max == 0) return null;
        return (cantidadActual * 100) / max;
    }
}
