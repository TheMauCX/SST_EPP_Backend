package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Tabla maestra de tallas disponibles en el sistema.
 *
 * Ejemplos de valores:
 *   Ropa:    S, M, L, XL, XXL, XXXL
 *   Calzado: 36, 37, 38, 39, 40, 41, 42, 43, 44
 *   Guantes: XS, S, M, L, XL
 *   Talla única: UNICA
 *
 * Hibernate crea la tabla automáticamente con ddl-auto=update.
 */
@Entity
@Table(name = "catalogo_talla", schema = "epp",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_catalogo_talla_nombre",
                columnNames = {"nombre"}
        ))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogoTalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "talla_id")
    private Integer tallaId;

    /**
     * Nombre de la talla. Ej: "S", "M", "L", "38", "UNICA"
     */
    @NotNull
    @Column(name = "nombre", nullable = false, unique = true, length = 20)
    private String nombre;

    /**
     * Descripción opcional. Ej: "Talla pequeña", "Número 38 europeo"
     */
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    /**
     * Orden de visualización en la UI (para mostrar S < M < L < XL)
     */
    @Column(name = "orden_visualizacion")
    private Integer ordenVisualizacion;
}
