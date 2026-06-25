package pe.edu.upeu.epp.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO estándar para respuestas de error.
 * Proporciona una estructura consistente para todos los errores de la API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp de cuando ocurrió el error.
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP.
     */
    private Integer status;

    /**
     * Tipo de error (ej: "Bad Request", "Not Found").
     */
    private String error;

    /**
     * Mensaje descriptivo del error.
     */
    private String message;

    /**
     * Detalles adicionales del error (opcional).
     * Por ejemplo, errores de validación campo por campo.
     */
    private Map<String, ?> details;

    /**
     * Path del endpoint que generó el error.
     */
    private String path;
}