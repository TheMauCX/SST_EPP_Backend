package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InventarioArea;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de Inventario por Área.
 *
 * @author Sistema EPP
 * @version 2.0
 */
@Repository
public interface InventarioAreaRepository extends JpaRepository<InventarioArea, Integer> {

    // ============================================
    // MÉTODOS CRÍTICOS PARA MERGE LOGIC
    // ============================================

    /**
     * Busca un inventario por combinación única (epp, área, estado).
     */
    Optional<InventarioArea> findByEppAndAreaAndEstado(
            CatalogoEpp epp,
            Area area,
            EstadoEpp estado
    );

    /**
     * Verifica si existe un inventario con la combinación (epp, área, estado).
     */
    boolean existsByEppAndAreaAndEstado(
            CatalogoEpp epp,
            Area area,
            EstadoEpp estado
    );

    // ============================================
    // CONSULTAS BÁSICAS
    // ============================================

    /**
     * Busca todos los inventarios de un área.
     */
    List<InventarioArea> findByArea(Area area);

    /**
     * Busca todos los inventarios de un EPP.
     */
    List<InventarioArea> findByEpp(CatalogoEpp epp);

    /**
     * Busca inventario de un EPP en un área específica.
     */
    List<InventarioArea> findByEppAndArea(CatalogoEpp epp, Area area);

    /**
     * Busca inventarios de un área que permiten uso.
     */
    @Query("SELECT ia FROM InventarioArea ia " +
            "WHERE ia.area = :area " +
            "AND ia.estado.permiteUso = :permiteUso " +
            "AND ia.cantidadActual > 0 " +
            "ORDER BY ia.epp.nombreEpp")
    List<InventarioArea> findByAreaAndEstadoPermiteUso(
            @Param("area") Area area,
            @Param("permiteUso") Boolean permiteUso
    );

    /**
     * Busca inventarios de un EPP en un área que permiten uso.
     */
    @Query("SELECT ia FROM InventarioArea ia " +
            "WHERE ia.area = :area " +
            "AND ia.epp = :epp " +
            "AND ia.estado.permiteUso = :permiteUso " +
            "AND ia.cantidadActual > 0 " +
            "ORDER BY ia.cantidadActual DESC")
    List<InventarioArea> findByAreaAndEppAndEstadoPermiteUso(
            @Param("area") Area area,
            @Param("epp") CatalogoEpp epp,
            @Param("permiteUso") Boolean permiteUso
    );

    /**
     * Encuentra inventarios con stock bajo en un área específica.
     */
    @Query("SELECT ia FROM InventarioArea ia " +
            "WHERE ia.area = :area " +
            "AND ia.cantidadActual <= ia.cantidadMinima " +
            "ORDER BY ia.epp.nombreEpp")
    List<InventarioArea> findStockBajoByArea(@Param("area") Area area);

    /**
     * Encuentra inventarios con stock bajo en todas las áreas.
     */
    @Query("SELECT ia FROM InventarioArea ia " +
            "WHERE ia.cantidadActual <= ia.cantidadMinima " +
            "ORDER BY ia.area.nombreArea, ia.epp.nombreEpp")
    List<InventarioArea> findStockBajo();

    /**
     * Encuentra inventarios por estado.
     */
    List<InventarioArea> findByEstado(EstadoEpp estado);

    /**
     * Encuentra inventarios de un EPP con un estado específico.
     */
    List<InventarioArea> findByEppAndEstado(CatalogoEpp epp, EstadoEpp estado);
}