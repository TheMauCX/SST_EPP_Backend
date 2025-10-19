package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InventarioCentral;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de Inventario Central.
 *
 * @author Sistema EPP
 * @version 2.0
 */
@Repository
public interface InventarioCentralRepository extends JpaRepository<InventarioCentral, Integer> {

    // ============================================
    // MÉTODOS CRÍTICOS PARA MERGE LOGIC
    // ============================================

    /**
     * Busca un inventario por combinación única (epp, lote, estado).
     */
    Optional<InventarioCentral> findByEppAndLoteAndEstado(
            CatalogoEpp epp,
            String lote,
            EstadoEpp estado
    );

    /**
     * Verifica si existe un inventario con la combinación (epp, lote, estado).
     */
    boolean existsByEppAndLoteAndEstado(
            CatalogoEpp epp,
            String lote,
            EstadoEpp estado
    );

    // ============================================
    // CONSULTAS BÁSICAS
    // ============================================

    /**
     * Busca todos los inventarios de un EPP específico.
     */
    List<InventarioCentral> findByEpp(CatalogoEpp epp);

    /**
     * Busca inventarios por EPP ordenados por cantidad disponible descendente.
     */
    List<InventarioCentral> findByEppOrderByCantidadActualDesc(CatalogoEpp epp);

    /**
     * Busca inventarios de un EPP que permiten uso, ordenados por cantidad.
     */
    @Query("SELECT ic FROM InventarioCentral ic " +
            "WHERE ic.epp = :epp " +
            "AND ic.estado.permiteUso = :permiteUso " +
            "AND ic.cantidadActual > 0 " +
            "ORDER BY ic.cantidadActual DESC")
    List<InventarioCentral> findByEppAndEstadoPermiteUsoOrderByCantidadDesc(
            @Param("epp") CatalogoEpp epp,
            @Param("permiteUso") Boolean permiteUso
    );

    /**
     * Encuentra inventarios con stock bajo (cantidad <= mínimo).
     */
    @Query("SELECT ic FROM InventarioCentral ic " +
            "WHERE ic.cantidadActual <= ic.cantidadMinima " +
            "ORDER BY ic.epp.nombreEpp")
    List<InventarioCentral> findStockBajo();

    /**
     * Encuentra inventarios que vencen antes de una fecha.
     */
    @Query("SELECT ic FROM InventarioCentral ic " +
            "WHERE ic.fechaVencimiento IS NOT NULL " +
            "AND ic.fechaVencimiento <= :fecha " +
            "AND ic.cantidadActual > 0 " +
            "ORDER BY ic.fechaVencimiento ASC")
    List<InventarioCentral> findByFechaVencimientoBefore(@Param("fecha") LocalDate fecha);

    /**
     * Encuentra inventarios por lote.
     */
    List<InventarioCentral> findByLote(String lote);

    /**
     * Encuentra inventarios por estado.
     */
    List<InventarioCentral> findByEstado(EstadoEpp estado);

    /**
     * Encuentra inventarios por EPP y estado.
     */
    List<InventarioCentral> findByEppAndEstado(CatalogoEpp epp, EstadoEpp estado);
}