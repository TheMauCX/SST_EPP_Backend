package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_central", schema = "epp",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventario_central_epp_lote_estado",
                columnNames = {"epp_id", "lote", "estado_id"}
        ),
        indexes = {
                @Index(name = "idx_inv_central_epp", columnList = "epp_id"),
                @Index(name = "idx_inv_central_estado", columnList = "estado_id"),
                @Index(name = "idx_inv_central_vencimiento", columnList = "fecha_vencimiento")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Min(0)
    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual = 0;

    @Min(0)
    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima = 0;

    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;

    @Column(name = "ubicacion_bodega", length = 100)
    private String ubicacionBodega;

    @Column(name = "lote", length = 50)
    private String lote;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "costo_unitario")
    private BigDecimal costoUnitario;

    @Column(name = "proveedor", length = 200)
    private String proveedor;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

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
}