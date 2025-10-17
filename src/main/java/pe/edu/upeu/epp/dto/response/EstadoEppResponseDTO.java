package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO de respuesta para EstadoEpp.
 * Retorna información de los estados disponibles para EPPs.
 *
 * @author Sistema EPP
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoEppResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID único del estado
     */
    private Integer estadoId;

    /**
     * Nombre del estado (ej: NUEVO, USADO, DAÑADO)
     */
    private String nombre;

    /**
     * Descripción detallada del estado
     */
    private String descripcion;

    /**
     * Indica si el EPP en este estado permite uso
     * true = Se puede usar/entregar
     * false = No se puede usar (dañado, en reparación, etc.)
     */
    private Boolean permiteUso;

    /**
     * Código hexadecimal del color para representación visual
     * Ejemplo: #28a745 (verde), #dc3545 (rojo)
     */
    private String colorHex;
}