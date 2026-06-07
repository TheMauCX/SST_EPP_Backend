package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Vista consolidada del inventario central.
 *
 * Agrupa registros con mismo (epp_id, proveedor, lote) para mostrar
 * en un solo apartado visual todas las tallas de un EPP de un mismo
 * lote/proveedor, con sus cantidades y costos unitarios.
 *
 * Ejemplo de respuesta:
 * {
 *   "eppId": 1,
 *   "eppNombre": "Casco 3M H-700",
 *   "lote": "F001-2025",
 *   "proveedor": "3M del Perú SAC",
 *   "fechaAdquisicion": "2025-11-01",
 *   "totalCantidad": 35,
 *   "cantidadMinima": 10,   ← del catálogo
 *   "necesitaReposicion": false,
 *   "detallesPorTalla": [
 *     { "tallaId": 3, "tallaNombre": "M", "cantidadActual": 20, "costoUnitario": 118.00 },
 *     { "tallaId": 4, "tallaNombre": "L", "cantidadActual": 15, "costoUnitario": 118.00 }
 *   ]
 * }
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioAgrupadoResponseDTO {

    private Integer eppId;
    private String  eppNombre;
    private CatalogoEpp.TipoUso tipoUso;
    private String  color;

    // Clave de agrupación
    private String lote;
    private String proveedor;
    private LocalDate fechaAdquisicion;
    private LocalDate fechaVencimiento;

    // Totales del grupo
    private Integer totalCantidad;

    // Umbrales del catálogo (aplican a todos los registros del grupo)
    private Integer cantidadMinima;
    private Integer cantidadMaxima;
    private Boolean necesitaReposicion;

    /** Una entrada por cada talla disponible en este lote/proveedor. */
    private List<DetalleTallaDTO> detallesPorTalla;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTallaDTO {
        private Integer inventarioId;   // inventario_central_id — para acciones individuales
        private Integer tallaId;
        private String  tallaNombre;
        private Integer cantidadActual;
        private BigDecimal costoUnitario;
        private String estadoNombre;
    }
}
