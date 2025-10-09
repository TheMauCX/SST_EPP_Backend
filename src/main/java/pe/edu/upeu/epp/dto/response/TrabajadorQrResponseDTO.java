package pe.edu.upeu.epp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO especializado para respuesta con código QR.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorQrResponseDTO {

    private Integer trabajadorId;
    private String nombreCompleto;
    private String dni;
    private String codigoQrPhotocheck;
    private String areaNombre;
    private String puesto;

    /**
     * URL para generar imagen del código QR.
     * Ejemplo: https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=QR-20251007-000001
     */
    private String qrImageUrl;
}
