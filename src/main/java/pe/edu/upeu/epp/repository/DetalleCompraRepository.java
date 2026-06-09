package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.DetalleCompra;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Integer> {

    /**
     * E — EPPs más comprados: cuántas veces aparece cada EPP en facturas distintas
     * y cuántas unidades se compraron en total.
     * Retorna: [eppId, eppNombre, vecesComprado, totalUnidadesCompradas, gastoTotalHistorico]
     */
    @Query("SELECT dc.epp.eppId, dc.epp.nombreEpp, " +
           "COUNT(DISTINCT dc.compra.compraId), " +
           "SUM(dc.cantidad), " +
           "SUM(dc.subtotal) " +
           "FROM DetalleCompra dc " +
           "GROUP BY dc.epp.eppId, dc.epp.nombreEpp " +
           "ORDER BY COUNT(DISTINCT dc.compra.compraId) DESC")
    List<Object[]> findEppsMasComprados();
}
