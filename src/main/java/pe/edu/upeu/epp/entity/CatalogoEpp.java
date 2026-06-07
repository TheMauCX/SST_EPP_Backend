package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Catálogo de tipos de EPP.
 *
 * Sprint 4b:
 *   - cantidadMinima y cantidadMaxima se mueven aquí desde InventarioCentral/InventarioArea.
 *     Son umbrales de alerta de stock propios del tipo de EPP, no del lote/área.
 */
@Entity
@Table(name = "catalogo_epp", schema = "epp", indexes = {
        @Index(name = "idx_catalogo_epp_tipo_uso", columnList = "tipo_uso"),
        @Index(name = "idx_catalogo_epp_activo",   columnList = "activo")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogoEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "epp_id")
    private Integer eppId;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre_epp", nullable = false, length = 100)
    private String nombreEpp;

    // ── Ficha Técnica ────────────────────────────────────────────────────────

    @Column(name = "aprobaciones_normas", length = 255)
    private String aprobacionesNormas;

    @Column(columnDefinition = "TEXT")
    private String caracteristicas;

    @Column(length = 100)
    private String fabricante;

    @Column(name = "tiempo_uso_fabricante", length = 150)
    private String tiempoUsoFabricante;

    @Column(name = "tiempo_uso_operacion", length = 150)
    private String tiempoUsoOperacion;

    @Column(name = "condiciones_mantenimiento", columnDefinition = "TEXT")
    private String condicionesMantenimiento;

    @Column(name = "condiciones_almacenamiento", columnDefinition = "TEXT")
    private String condicionesAlmacenamiento;

    @Column(name = "condiciones_cambio_prematuro", columnDefinition = "TEXT")
    private String condicionesCambioPrematuro;

    @Column(name = "foto_referencia", length = 500)
    private String fotoReferencia;

    @Column(name = "ficha_tecnica_path", length = 500)
    private String fichaTecnicaPath;

    @Column(name = "color", length = 60)
    private String color;

    // ── Umbrales de stock (antes estaban en inventario_central e inventario_area) ─

    /**
     * Cantidad mínima de alerta para este tipo de EPP.
     * Si el stock en central o área cae a este nivel, se activa la alerta.
     * Sprint 4b: migrado desde inventario_central / inventario_area.
     */
    @Min(0)
    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima = 0;

    /**
     * Cantidad máxima recomendada en stock para este tipo de EPP.
     * Opcional; se usa para calcular el porcentaje de stock.
     */
    @Min(0)
    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;

    // ── Tallas disponibles ───────────────────────────────────────────────────

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "epp_talla",
            schema = "epp",
            joinColumns        = @JoinColumn(name = "epp_id"),
            inverseJoinColumns = @JoinColumn(name = "talla_id")
    )
    @Builder.Default
    private Set<CatalogoTalla> tallasDisponibles = new HashSet<>();

    // ── Auditoría ────────────────────────────────────────────────────────────

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "activo")
    private Boolean activo = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_uso", nullable = false, length = 20)
    private TipoUso tipoUso;

    @PrePersist
    protected void onCreate() {
        fechaCreacion      = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) activo = true;
        if (cantidadMinima == null) cantidadMinima = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum TipoUso { CONSUMIBLE, DURADERO }
}
