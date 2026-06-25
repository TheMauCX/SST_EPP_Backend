package pe.edu.upeu.epp.dto.response;

import lombok.*;
import pe.edu.upeu.epp.entity.CatalogoEpp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista consolidada del inventario de área.
 *
 * Agrupa registros con mismo (epp_id, area_id) para mostrar
 * todas las tallas de un EPP en un área en un solo objeto visual.
 *
 * Ejemplo:
 * {
 *   "eppId": 1, "eppNombre": "Casco 3M",
 *   "areaId": 2, "areaNombre": "Producción",
 *   "totalCantidad": 8,
 *   "necesitaReposicion": false,
 *   "detallesPorTalla": [
 *     { "tallaId": 3, "tallaNombre": "M", "cantidadActual": 5 },
 *     { "tallaId": 4, "tallaNombre": "L", "cantidadActual": 3 }
 *   ]
 * }
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioAreaAgrupadoResponseDTO {

    private Integer eppId;
    private String  eppNombre;
    private CatalogoEpp.TipoUso tipoUso;
    private String  color;

    private Integer areaId;
    private String  areaNombre;

    private LocalDateTime ultimaActualizacion;

    // Totales del grupo
    private Integer totalCantidad;

    // Umbrales del catálogo
    private Integer cantidadMinima;
    private Integer cantidadMaxima;
    private Boolean necesitaReposicion;

    /** Una entrada por cada talla disponible para este EPP en esta área. */
    private List<DetalleTallaDTO> detallesPorTalla;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTallaDTO {
        private Integer inventarioAreaId;  // para acciones individuales
        private Integer tallaId;
        private String  tallaNombre;
        private Integer cantidadActual;
        private String  estadoNombre;
        private String  ubicacion;
    }
}
