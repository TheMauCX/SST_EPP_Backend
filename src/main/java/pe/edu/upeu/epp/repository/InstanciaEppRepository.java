package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InstanciaEpp;
import pe.edu.upeu.epp.entity.Trabajador;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// ==================== INSTANCIA EPP REPOSITORY ====================
@Repository
public interface InstanciaEppRepository extends JpaRepository<InstanciaEpp, Integer> {
    Optional<InstanciaEpp> findByCodigoSerie(String codigoSerie);
    List<InstanciaEpp> findByEstado(EstadoEpp estado);
    List<InstanciaEpp> findByTrabajadorActual(Trabajador trabajador);
    List<InstanciaEpp> findByAreaActual(Area area);

    @Query("SELECT ie FROM InstanciaEpp ie WHERE ie.estado.nombre = 'EN_STOCK' AND ie.epp.eppId = :eppId")
    List<InstanciaEpp> findDisponiblesPorEpp(@Param("eppId") Integer eppId);

    @Query("SELECT ie FROM InstanciaEpp ie WHERE ie.fechaVencimiento < :fecha AND ie.estado.nombre != 'BAJA'")
    List<InstanciaEpp> findVencidos(@Param("fecha") LocalDate fecha);

    @Query("SELECT ie FROM InstanciaEpp ie WHERE ie.fechaProximaInspeccion <= :fecha AND ie.estado.nombre != 'BAJA'")
    List<InstanciaEpp> findProximasInspeccion(@Param("fecha") LocalDate fecha);

    @Query("SELECT ie FROM InstanciaEpp ie WHERE ie.trabajadorActual.trabajadorId = :trabajadorId")
    List<InstanciaEpp> findByTrabajadorActualId(@Param("trabajadorId") Integer trabajadorId);
}