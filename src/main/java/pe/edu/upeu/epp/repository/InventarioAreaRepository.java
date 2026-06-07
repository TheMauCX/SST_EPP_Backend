package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioAreaRepository extends JpaRepository<InventarioArea, Integer> {

    // ── Unicidad (clave ampliada con talla) ──────────────────────────────

    Optional<InventarioArea> findByEppAndAreaAndEstadoAndTalla(
            CatalogoEpp epp, Area area, EstadoEpp estado, CatalogoTalla talla);

    boolean existsByEppAndAreaAndEstadoAndTalla(
            CatalogoEpp epp, Area area, EstadoEpp estado, CatalogoTalla talla);

    /**
     * Para EPPs sin talla (talla = null).
     */
    @Query("SELECT ia FROM InventarioArea ia " +
           "WHERE ia.epp = :epp AND ia.area = :area AND ia.estado = :estado AND ia.talla IS NULL")
    Optional<InventarioArea> findByEppAndAreaAndEstadoSinTalla(
            @Param("epp") CatalogoEpp epp,
            @Param("area") Area area,
            @Param("estado") EstadoEpp estado);

    // ── Mantenemos compatibilidad (usado en EntregaEppService) ───────────
    // Deprecated: usar findByEppAndAreaAndEstadoAndTalla o findByEppAndAreaAndEstadoSinTalla
    Optional<InventarioArea> findByEppAndAreaAndEstado(CatalogoEpp epp, Area area, EstadoEpp estado);

    boolean existsByEppAndAreaAndEstado(CatalogoEpp epp, Area area, EstadoEpp estado);

    // ── Consultas básicas ────────────────────────────────────────────────

    List<InventarioArea> findByArea(Area area);

    List<InventarioArea> findByEpp(CatalogoEpp epp);

    List<InventarioArea> findByEppAndArea(CatalogoEpp epp, Area area);

    @Query("SELECT ia FROM InventarioArea ia " +
           "WHERE ia.area = :area AND ia.estado.permiteUso = :permiteUso AND ia.cantidadActual > 0 " +
           "ORDER BY ia.epp.nombreEpp")
    List<InventarioArea> findByAreaAndEstadoPermiteUso(
            @Param("area") Area area,
            @Param("permiteUso") Boolean permiteUso);

    @Query("SELECT ia FROM InventarioArea ia " +
           "WHERE ia.area = :area AND ia.epp = :epp AND ia.estado.permiteUso = :permiteUso " +
           "AND ia.cantidadActual > 0 ORDER BY ia.cantidadActual DESC")
    List<InventarioArea> findByAreaAndEppAndEstadoPermiteUso(
            @Param("area") Area area,
            @Param("epp") CatalogoEpp epp,
            @Param("permiteUso") Boolean permiteUso);

    // ── Stock bajo ───────────────────────────────────────────────────────

    @Query("SELECT ia FROM InventarioArea ia " +
            "WHERE ia.area = :area AND ia.cantidadActual <= ia.epp.cantidadMinima")
    List<InventarioArea> findStockBajoByArea(@Param("area") Area area);

    @Query("SELECT ia FROM InventarioArea ia " +
            "WHERE ia.cantidadActual <= ia.epp.cantidadMinima")
    List<InventarioArea> findStockBajo();

    List<InventarioArea> findByEstado(EstadoEpp estado);

    List<InventarioArea> findByEppAndEstado(CatalogoEpp epp, EstadoEpp estado);
}
