package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.EntregaEpp;
import pe.edu.upeu.epp.entity.Trabajador;

import java.time.LocalDateTime;
import java.util.List;

// ==================== ENTREGA EPP REPOSITORY ====================
@Repository
public interface EntregaEppRepository extends JpaRepository<EntregaEpp, Integer> {
    List<EntregaEpp> findByTrabajador(Trabajador trabajador);
    List<EntregaEpp> findByJefeArea(Trabajador jefeArea);
    List<EntregaEpp> findByFechaEntregaBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT e FROM EntregaEpp e JOIN FETCH e.detalles WHERE e.trabajador.trabajadorId = :trabajadorId ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findHistorialByTrabajadorId(@Param("trabajadorId") Integer trabajadorId);

    @Query("SELECT e FROM EntregaEpp e WHERE e.trabajador.area.areaId = :areaId AND e.fechaEntrega BETWEEN :fechaInicio AND :fechaFin")
    List<EntregaEpp> findByAreaAndFechaRange(@Param("areaId") Integer areaId,
                                             @Param("fechaInicio") LocalDateTime fechaInicio,
                                             @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT e FROM EntregaEpp e WHERE e.fechaEntrega BETWEEN :fechaInicio AND :fechaFin")
    List<EntregaEpp> findByFechaRange(@Param("fechaInicio") LocalDateTime fechaInicio,
                                      @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COUNT(e) FROM EntregaEpp e WHERE DATE(e.fechaEntrega) = CURRENT_DATE")
    long countEntregasHoy();
}