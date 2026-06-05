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

    // ── Métodos de unicidad (clave ampliada con talla) ───────────────────

    Optional<InventarioCentral> findByEppAndLoteAndEstadoAndTalla(
            CatalogoEpp epp, String lote, EstadoEpp estado, CatalogoTalla talla);

    boolean existsByEppAndLoteAndEstadoAndTalla(
            CatalogoEpp epp, String lote, EstadoEpp estado, CatalogoTalla talla);

    /**
     * Compatibilidad con EPPs sin talla (talla = null).
     * JPQL no puede usar findBy...AndTallaIsNull directamente con parámetros,
     * por eso se usa @Query explícita.
     */
    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.epp = :epp AND ic.lote = :lote AND ic.estado = :estado AND ic.talla IS NULL")
    Optional<InventarioCentral> findByEppAndLoteAndEstadoSinTalla(
            @Param("epp") CatalogoEpp epp,
            @Param("lote") String lote,
            @Param("estado") EstadoEpp estado);

    // ── Consultas básicas ────────────────────────────────────────────────

    List<InventarioCentral> findByEpp(CatalogoEpp epp);

    List<InventarioCentral> findByEppOrderByCantidadActualDesc(CatalogoEpp epp);

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.epp = :epp AND ic.estado.permiteUso = :permiteUso AND ic.cantidadActual > 0 " +
           "ORDER BY ic.cantidadActual DESC")
    List<InventarioCentral> findByEppAndEstadoPermiteUsoOrderByCantidadDesc(
            @Param("epp") CatalogoEpp epp,
            @Param("permiteUso") Boolean permiteUso);

    // ── Stock bajo ───────────────────────────────────────────────────────

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.cantidadActual <= ic.cantidadMinima ORDER BY ic.epp.nombreEpp")
    List<InventarioCentral> findStockBajo();

    // ── Vencimiento ──────────────────────────────────────────────────────

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.fechaVencimiento IS NOT NULL AND ic.fechaVencimiento <= :fecha AND ic.cantidadActual > 0 " +
           "ORDER BY ic.fechaVencimiento ASC")
    List<InventarioCentral> findByFechaVencimientoBefore(@Param("fecha") LocalDate fecha);

    List<InventarioCentral> findByLote(String lote);

    List<InventarioCentral> findByEstado(EstadoEpp estado);

    List<InventarioCentral> findByEppAndEstado(CatalogoEpp epp, EstadoEpp estado);

    // ── HU-24: Inventario central ordenado alfabéticamente ──────────────

    @Query("SELECT ic FROM InventarioCentral ic ORDER BY ic.epp.nombreEpp ASC")
    Page<InventarioCentral> findAllOrderByNombreEppAsc(Pageable pageable);

    // ── HU-24: Inventario central filtrado por bajo stock ────────────────

    @Query("SELECT ic FROM InventarioCentral ic " +
           "WHERE ic.cantidadActual <= ic.cantidadMinima ORDER BY ic.epp.nombreEpp ASC")
    Page<InventarioCentral> findStockBajoPaginado(Pageable pageable);

    // ── HU-14: Reporte de gastos — suma de entregas por mes ─────────────

    /**
     * Calcula el gasto mensual: SUM(cantidad_entregada * costo_unitario).
     * Cruza DetalleEntregaEpp con la compra más reciente del EPP para
     * obtener el precio unitario aproximado.
     * Retorna: [año (Integer), mes (Integer), totalGasto (BigDecimal)]
     */
    @Query("SELECT YEAR(e.fechaEntrega), MONTH(e.fechaEntrega), " +
           "SUM(d.cantidad * ic.costoUnitario) " +
           "FROM DetalleEntregaEpp d " +
           "JOIN d.entrega e " +
           "JOIN InventarioCentral ic ON ic.epp = d.epp " +
           "WHERE (:areaId IS NULL OR e.trabajador.area.areaId = :areaId) " +
           "GROUP BY YEAR(e.fechaEntrega), MONTH(e.fechaEntrega) " +
           "ORDER BY YEAR(e.fechaEntrega) ASC, MONTH(e.fechaEntrega) ASC")
    List<Object[]> calcularGastoMensual(@Param("areaId") Integer areaId);

    // ── HU-15: Rotación — EPPs más entregados ───────────────────────────

    /**
     * Retorna los EPPs con más salidas (entregas) en el período dado.
     * [eppId (Integer), nombreEpp (String), totalEntregado (Long)]
     */
    @Query("SELECT d.epp.eppId, d.epp.nombreEpp, SUM(d.cantidad) as totalEntregado " +
           "FROM DetalleEntregaEpp d " +
           "WHERE d.entrega.fechaEntrega >= :desde " +
           "GROUP BY d.epp.eppId, d.epp.nombreEpp " +
           "ORDER BY totalEntregado DESC")
    List<Object[]> findEppsMasEntregados(@Param("desde") java.time.LocalDateTime desde);

    /**
     * Retorna los EPPs que NO tuvieron ninguna salida en el período dado
     * (inmovilizados): están en el catálogo pero sin entregas.
     * [eppId (Integer), nombreEpp (String)]
     */
    @Query("SELECT c.eppId, c.nombreEpp FROM CatalogoEpp c " +
           "WHERE c.activo = true " +
           "AND c.eppId NOT IN (" +
           "  SELECT DISTINCT d.epp.eppId FROM DetalleEntregaEpp d " +
           "  WHERE d.entrega.fechaEntrega >= :desde" +
           ")")
    List<Object[]> findEppsInmovilizados(@Param("desde") java.time.LocalDateTime desde);
}
