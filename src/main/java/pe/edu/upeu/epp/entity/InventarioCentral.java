package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_central", schema = "epp",
        // CORREGIDO: Hibernate necesita los nombres exactos de las columnas SQL
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventario_central_epp_lote",
                columnNames = {"epp_id", "lote"}
        ),
        indexes = {
                @Index(name = "idx_inv_central_epp", columnList = "epp_id"),
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
    private java.time.LocalDate fechaAdquisicion;

    @Column(name = "costo_unitario")
    private java.math.BigDecimal costoUnitario;

    @Column(name = "proveedor", length = 200)
    private String proveedor;

    @Column(name = "fecha_vencimiento")
    private java.time.LocalDate fechaVencimiento;

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
}