package pe.edu.upeu.epp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.CatalogoTalla;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InventarioCentral;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioCentralRepository extends JpaRepository<InventarioCentral, Integer> {

    // ── Unicidad ─────────────────────────────────────────────────────────────

    Optional<InventarioCentral> findByEppAndLoteAndEstadoAndTalla(
            CatalogoEpp epp, String lote, EstadoEpp estado, CatalogoTalla talla);

    boolean existsByEppAndLoteAndEstadoAndTalla(
            CatalogoEpp epp, String lote, EstadoEpp estado, CatalogoTalla talla);

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.epp = :epp AND ic.lote = :lote AND ic.estado = :estado AND ic.talla IS NULL")
    Optional<InventarioCentral> findByEppAndLoteAndEstadoSinTalla(
            @Param("epp") CatalogoEpp epp,
            @Param("lote") String lote,
            @Param("estado") EstadoEpp estado);

    // ── Consultas básicas ────────────────────────────────────────────────────

    List<InventarioCentral> findByEpp(CatalogoEpp epp);

    List<InventarioCentral> findByEppOrderByCantidadActualDesc(CatalogoEpp epp);

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.epp = :epp AND ic.estado.permiteUso = :permiteUso AND ic.cantidadActual > 0 " +
           "ORDER BY ic.cantidadActual DESC")
    List<InventarioCentral> findByEppAndEstadoPermiteUsoOrderByCantidadDesc(
            @Param("epp") CatalogoEpp epp,
            @Param("permiteUso") Boolean permiteUso);

    // ── Stock bajo ───────────────────────────────────────────────────────────

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.cantidadActual <= ic.epp.cantidadMinima ORDER BY ic.epp.nombreEpp")
    List<InventarioCentral> findStockBajo();

    // ── Vencimiento ──────────────────────────────────────────────────────────

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.fechaVencimiento IS NOT NULL AND ic.fechaVencimiento <= :fecha AND ic.cantidadActual > 0 " +
           "ORDER BY ic.fechaVencimiento ASC")
    List<InventarioCentral> findByFechaVencimientoBefore(@Param("fecha") LocalDate fecha);

    List<InventarioCentral> findByLote(String lote);
    List<InventarioCentral> findByEstado(EstadoEpp estado);
    List<InventarioCentral> findByEppAndEstado(CatalogoEpp epp, EstadoEpp estado);

    // ── Paginados ─────────────────────────────────────────────────────────────

    @Query("SELECT ic FROM InventarioCentral ic ORDER BY ic.epp.nombreEpp ASC")
    Page<InventarioCentral> findAllOrderByNombreEppAsc(Pageable pageable);

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.cantidadActual <= ic.epp.cantidadMinima ORDER BY ic.epp.nombreEpp ASC")
    Page<InventarioCentral> findStockBajoPaginado(Pageable pageable);

    // ── HU-14: Gastos mensuales ───────────────────────────────────────────────

    @Query("SELECT YEAR(e.fechaEntrega), MONTH(e.fechaEntrega), " +
           "SUM(d.cantidad * ic.costoUnitario) " +
           "FROM DetalleEntregaEpp d " +
           "JOIN d.entrega e " +
           "JOIN InventarioCentral ic ON ic.epp = d.epp " +
           "WHERE (:areaId IS NULL OR e.trabajador.area.areaId = :areaId) " +
           "GROUP BY YEAR(e.fechaEntrega), MONTH(e.fechaEntrega) " +
           "ORDER BY YEAR(e.fechaEntrega) ASC, MONTH(e.fechaEntrega) ASC")
    List<Object[]> calcularGastoMensual(@Param("areaId") Integer areaId);

    // ── HU-15: Rotación ───────────────────────────────────────────────────────

    @Query("SELECT d.epp.eppId, d.epp.nombreEpp, SUM(d.cantidad) as totalEntregado " +
           "FROM DetalleEntregaEpp d " +
           "WHERE d.entrega.fechaEntrega >= :desde " +
           "GROUP BY d.epp.eppId, d.epp.nombreEpp " +
           "ORDER BY totalEntregado DESC")
    List<Object[]> findEppsMasEntregados(@Param("desde") java.time.LocalDateTime desde);

    @Query("SELECT c.eppId, c.nombreEpp FROM CatalogoEpp c " +
           "WHERE c.activo = true " +
           "AND c.eppId NOT IN (" +
           "  SELECT DISTINCT d.epp.eppId FROM DetalleEntregaEpp d " +
           "  WHERE d.entrega.fechaEntrega >= :desde" +
           ")")
    List<Object[]> findEppsInmovilizados(@Param("desde") java.time.LocalDateTime desde);

    // ── ESTADÍSTICAS NUEVAS ────────────────────────────────────────────────────

    /**
     * A — Precio unitario actual de cada EPP.
     * Toma el costo unitario del lote más reciente (por fecha de adquisición).
     * Retorna: [eppId, eppNombre, costoUnitario, fechaAdquisicion, proveedor]
     */
    @Query("SELECT ic.epp.eppId, ic.epp.nombreEpp, ic.costoUnitario, " +
           "ic.fechaAdquisicion, ic.proveedor " +
           "FROM InventarioCentral ic " +
           "WHERE ic.costoUnitario IS NOT NULL " +
           "AND ic.fechaAdquisicion = (" +
           "  SELECT MAX(ic2.fechaAdquisicion) FROM InventarioCentral ic2 " +
           "  WHERE ic2.epp = ic.epp AND ic2.costoUnitario IS NOT NULL" +
           ") " +
           "GROUP BY ic.epp.eppId, ic.epp.nombreEpp, ic.costoUnitario, " +
           "         ic.fechaAdquisicion, ic.proveedor " +
           "ORDER BY ic.costoUnitario DESC")
    List<Object[]> findPrecioActualPorEpp();

    /**
     * B — Historial de precios de un EPP a lo largo del tiempo.
     * Cada registro de inventario creado al registrar una compra es un punto histórico.
     * Retorna: [lote, costoUnitario, fechaAdquisicion, proveedor]
     */
    @Query("SELECT ic.lote, ic.costoUnitario, ic.fechaAdquisicion, ic.proveedor " +
           "FROM InventarioCentral ic " +
           "WHERE ic.epp.eppId = :eppId AND ic.costoUnitario IS NOT NULL " +
           "GROUP BY ic.lote, ic.costoUnitario, ic.fechaAdquisicion, ic.proveedor " +
           "ORDER BY ic.fechaAdquisicion ASC")
    List<Object[]> findHistorialPreciosByEpp(@Param("eppId") Integer eppId);

    /**
     * C — Valorización del inventario central.
     * Retorna: [eppId, eppNombre, totalUnidades, costoUnitario, valorTotal]
     * El costoUnitario usado es MAX (precio del lote más caro disponible).
     * Para valorización conservadora usar AVG en su lugar.
     */
    @Query("SELECT ic.epp.eppId, ic.epp.nombreEpp, " +
           "SUM(ic.cantidadActual), " +
           "MAX(ic.costoUnitario), " +
           "SUM(ic.cantidadActual * ic.costoUnitario) " +
           "FROM InventarioCentral ic " +
           "WHERE ic.costoUnitario IS NOT NULL AND ic.cantidadActual > 0 " +
           "GROUP BY ic.epp.eppId, ic.epp.nombreEpp " +
           "ORDER BY SUM(ic.cantidadActual * ic.costoUnitario) DESC")
    List<Object[]> calcularValorInventarioCentral();
}
