package pe.edu.upeu.epp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "estado_epp", schema = "epp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estado_id")
    private Integer estadoId;

    @NotNull
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "permite_uso")
    private Boolean permiteUso = false;

    @Column(name = "color_hex", length = 7)
    private String colorHex;
}