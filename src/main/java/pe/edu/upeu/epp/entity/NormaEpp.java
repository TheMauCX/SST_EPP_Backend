package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Catálogo de normas de seguridad (ANSI, ISO, NTP, EN, NIOSH…).
 *
 * Cada norma tiene un código único, descripción detallada para mostrar
 * en un desplegable en la UI, y un icono opcional (SVG/PNG en Azure).
 *
 * Sprint 5 — HU-18 Catálogo de Normas Seleccionable.
 */
@Entity
@Table(name = "norma_epp", schema = "epp",
        indexes = {
                @Index(name = "idx_norma_organismo", columnList = "organismo"),
                @Index(name = "idx_norma_categoria", columnList = "categoria"),
                @Index(name = "idx_norma_activo",    columnList = "activo")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NormaEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "norma_id")
    private Integer normaId;

    /** Código oficial de la norma: "ANSI/ISEA Z89.1", "NTP-ISO 3873", etc. */
    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    /** Nombre descriptivo corto para mostrar en el selector. */
    @NotBlank
    @Size(max = 80)
    @Column(name = "nombre_corto", nullable = false, length = 80)
    private String nombreCorto;

    /**
     * Descripción completa para el panel desplegable.
     * Incluye qué certifica la norma, los niveles y qué significa cada número.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /** Organismo emisor: ANSI, ISO, NTP, NIOSH, EN, OSHA, GB, AS_NZS, OTRO. */
    @Enumerated(EnumType.STRING)
    @Column(name = "organismo", nullable = false, length = 50)
    private Organismo organismo;

    /** Parte del cuerpo / tipo de protección que cubre. */
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 50)
    private Categoria categoria;

    /**
     * URL del icono/emblema en Azure Blob Storage.
     * Null en entorno de desarrollo — el frontend muestra el código textual como fallback.
     */
    @Size(max = 500)
    @Column(name = "icono_url", length = 500)
    private String iconoUrl;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum Organismo {
        ANSI, ISO, NTP, NIOSH, EN, OSHA, GB, AS_NZS, OTRO
    }

    public enum Categoria {
        CABEZA, OJOS, OIDOS, RESPIRATORIA, MANOS, PIES, CAIDAS, CUERPO, SENALIZACION, MULTIPLE
    }
}
