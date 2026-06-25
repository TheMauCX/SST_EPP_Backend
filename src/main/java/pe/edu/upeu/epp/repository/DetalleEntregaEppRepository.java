package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.DetalleEntregaEpp;
import pe.edu.upeu.epp.entity.EntregaEpp;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleEntregaEppRepository extends JpaRepository<DetalleEntregaEpp, Integer> {

    List<DetalleEntregaEpp> findByEntrega(EntregaEpp entrega);
    List<DetalleEntregaEpp> findByEpp(CatalogoEpp epp);

    @Query("SELECT dee FROM DetalleEntregaEpp dee WHERE dee.entrega.trabajador.trabajadorId = :trabajadorId")
    List<DetalleEntregaEpp> findByTrabajadorId(@Param("trabajadorId") Integer trabajadorId);

    @Query("SELECT dee.epp.nombreEpp, COUNT(dee) as cantidad FROM DetalleEntregaEpp dee " +
           "WHERE dee.entrega.fechaEntrega BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY dee.epp.nombreEpp ORDER BY cantidad DESC")
    List<Object[]> findEppsMasEntregados(@Param("fechaInicio") LocalDateTime fechaInicio,
                                         @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * D — Trabajadores con mayor gasto acumulado en EPPs recibidos.
     *
     * Estrategia: se cruza el detalle de entrega con inventario_central
     * usando la fecha de adquisición más alta disponible para ese EPP.
     * Se usa AVG del costo unitario del inventario central cuando hay
     * múltiples lotes (evita la limitación de LIMIT en JPQL).
     *
     * Retorna: [trabajadorId, nombreCompleto, dni, areaNombre, totalEntregas, valorAcumulado]
     */
    @Query("SELECT e.trabajador.trabajadorId, " +
           "CONCAT(e.trabajador.nombres, ' ', e.trabajador.apellidos), " +
           "e.trabajador.dni, " +
           "e.trabajador.area.nombreArea, " +
           "COUNT(DISTINCT e.entregaId), " +
           "SUM(d.cantidad * (" +
           "  SELECT AVG(ic.costoUnitario) FROM InventarioCentral ic " +
           "  WHERE ic.epp = d.epp " +
           "  AND ic.costoUnitario IS NOT NULL " +
           "  AND ic.fechaAdquisicion = (" +
           "    SELECT MAX(ic2.fechaAdquisicion) FROM InventarioCentral ic2 " +
           "    WHERE ic2.epp = d.epp AND ic2.costoUnitario IS NOT NULL" +
           "  )" +
           ")) " +
           "FROM DetalleEntregaEpp d " +
           "JOIN d.entrega e " +
           "GROUP BY e.trabajador.trabajadorId, " +
           "         e.trabajador.nombres, e.trabajador.apellidos, " +
           "         e.trabajador.dni, e.trabajador.area.nombreArea " +
           "ORDER BY SUM(d.cantidad * (" +
           "  SELECT AVG(ic.costoUnitario) FROM InventarioCentral ic " +
           "  WHERE ic.epp = d.epp AND ic.costoUnitario IS NOT NULL " +
           "  AND ic.fechaAdquisicion = (" +
           "    SELECT MAX(ic2.fechaAdquisicion) FROM InventarioCentral ic2 " +
           "    WHERE ic2.epp = d.epp AND ic2.costoUnitario IS NOT NULL" +
           "  )" +
           ")) DESC")
    List<Object[]> findTrabajadoresMayorGasto();
}
