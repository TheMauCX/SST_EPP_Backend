package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Catálogo de tipos de EPP.
 *
 * Sprint 4 — cambios:
 *   - Se agrega campo 'color' (HU-17)
 *   - Se agrega relación Many-to-Many con CatalogoTalla (HU-17)
 *   - Se agrega campo 'ficha_tecnica_path' para PDF en Azure (HU-19)
 *
 * Hibernate aplica los cambios con ddl-auto=update (ALTER TABLE).
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

    // ── Campos de Ficha Técnica ──────────────────────────────────────────

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

    /** URL pública de la foto de referencia en Azure Blob Storage */
    @Column(name = "foto_referencia", length = 500)
    private String fotoReferencia;

    // ── Sprint 4: nuevos campos ──────────────────────────────────────────

    /**
     * Color del EPP. Ej: "Naranja", "Amarillo reflectivo", "Blanco".
     * Campo de texto libre porque los colores de EPP son descriptivos,
     * no necesitan tabla maestra.
     * HU-17
     */
    @Column(name = "color", length = 60)
    private String color;

    /**
     * Ruta/URL del archivo PDF de ficha técnica en Azure Blob Storage.
     * Almacena la URL pública del blob, no el contenido binario.
     * Ejemplo: https://account.blob.core.windows.net/media/epps/fichas/12-ficha.pdf
     * HU-19
     */
    @Column(name = "ficha_tecnica_path", length = 500)
    private String fichaTecnicaPath;

    /**
     * Tallas disponibles para este EPP.
     * Relación Many-to-Many con tabla maestra CatalogoTalla.
     * La tabla de unión 'epp_talla' se crea automáticamente con Hibernate.
     * Si el EPP no maneja tallas (ej. guantes de nitrilo caja x100),
     * la colección queda vacía y se usa talla "UNICA" en inventario.
     * HU-17
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "epp_talla",
            schema = "epp",
            joinColumns        = @JoinColumn(name = "epp_id"),
            inverseJoinColumns = @JoinColumn(name = "talla_id")
    )
    @Builder.Default
    private Set<CatalogoTalla> tallasDisponibles = new HashSet<>();

    // ── Auditoría y estado ───────────────────────────────────────────────

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
        fechaCreacion     = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum TipoUso {
        CONSUMIBLE, DURADERO
    }
}
