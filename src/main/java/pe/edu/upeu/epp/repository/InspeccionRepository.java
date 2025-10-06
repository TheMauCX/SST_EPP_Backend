package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Inspeccion;
import pe.edu.upeu.epp.entity.InstanciaEpp;
import pe.edu.upeu.epp.entity.Trabajador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ==================== INSPECCION REPOSITORY ====================
@Repository
public interface InspeccionRepository extends JpaRepository<Inspeccion, Integer> {
    List<Inspeccion> findByInstanciaEpp(InstanciaEpp instanciaEpp);
    List<Inspeccion> findByInspector(Trabajador inspector);
    List<Inspeccion> findByResultado(Inspeccion.ResultadoInspeccion resultado);

    @Query("SELECT i FROM Inspeccion i WHERE i.fechaProximaInspeccion <= :fecha")
    List<Inspeccion> findInspeccionesPendientes(@Param("fecha") LocalDate fecha);

    @Query("SELECT i FROM Inspeccion i WHERE i.instanciaEpp.trabajadorActual.trabajadorId = :trabajadorId " +
            "AND i.fechaInspeccion >= :fechaDesde ORDER BY i.fechaInspeccion DESC")
    List<Inspeccion> findByTrabajadorLast6Months(@Param("trabajadorId") Integer trabajadorId,
                                                 @Param("fechaDesde") LocalDateTime fechaDesde);

    @Query("SELECT i FROM Inspeccion i WHERE i.instanciaEpp.instanciaEppId = :instanciaId ORDER BY i.fechaInspeccion DESC")
    List<Inspeccion> findByInstanciaEppIdOrderByFechaDesc(@Param("instanciaId") Integer instanciaId);
}