package pe.edu.upeu.epp.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO devuelto por GET /api/v1/compras/documentos?nroFactura={lote}
 * Contiene las URLs de los archivos subidos a Azure para esa compra.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CompraDocumentosDTO {

    private Integer compraId;
    private String  nroFactura;
    private String  proveedor;
    private LocalDate fechaCompra;

    /** URL pública en Azure del archivo de factura. Null si no se subió. */
    private String urlFactura;

    /** URL pública en Azure del archivo de cotización. Null si no se subió. */
    private String urlCotizacion;

    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal montoTotal;
}