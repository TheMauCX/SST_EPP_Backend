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

// ==================== DETALLE ENTREGA REPOSITORY ====================
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
}