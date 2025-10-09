package pe.edu.upeu.epp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.EntregaEpp;
import pe.edu.upeu.epp.entity.Trabajador;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad EntregaEpp.
 * CRÍTICO: Gestiona las entregas de EPP a trabajadores.
 */
@Repository
public interface EntregaEppRepository extends JpaRepository<EntregaEpp, Integer> {

    /**
     * Obtener historial de entregas de un trabajador con paginación.
     * Se usa en el perfil del trabajador para ver su historial completo.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.trabajador.trabajadorId = :trabajadorId " +
            "ORDER BY e.fechaEntrega DESC")
    Page<EntregaEpp> findByTrabajador(@Param("trabajadorId") Integer trabajadorId, Pageable pageable);

    /**
     * Versión sin paginación para reportes.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.trabajador = :trabajador " +
            "ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findByTrabajador(@Param("trabajador") Trabajador trabajador);

    /**
     * Obtener entregas realizadas por un jefe de área.
     * Útil para auditoría y seguimiento de quien entrega.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.jefeArea.trabajadorId = :jefeAreaId " +
            "ORDER BY e.fechaEntrega DESC")
    Page<EntregaEpp> findByJefeArea(@Param("jefeAreaId") Integer jefeAreaId, Pageable pageable);

    /**
     * Obtener entregas en un rango de fechas.
     * Se usa para reportes mensuales/anuales.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.fechaEntrega BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findByFechaEntregaBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Obtener historial de entregas de un trabajador en un rango de fechas.
     * Útil para reportes de un trabajador específico en un periodo.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.trabajador.trabajadorId = :trabajadorId " +
            "AND e.fechaEntrega BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findHistorialByTrabajadorId(
            @Param("trabajadorId") Integer trabajadorId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Obtener entregas de un área en un rango de fechas.
     * Se usa para reportes por área.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.trabajador.area.areaId = :areaId " +
            "AND e.fechaEntrega BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findByAreaAndFechaRange(
            @Param("areaId") Integer areaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Contar entregas realizadas hoy.
     * Se usa en el dashboard para mostrar actividad del día.
     */
    @Query("SELECT COUNT(e) FROM EntregaEpp e WHERE DATE(e.fechaEntrega) = CURRENT_DATE")
    Long countEntregasHoy();

    /**
     * Contar entregas de un área hoy.
     * Dashboard del jefe de área.
     */
    @Query("SELECT COUNT(e) FROM EntregaEpp e " +
            "WHERE e.trabajador.area.areaId = :areaId " +
            "AND DATE(e.fechaEntrega) = CURRENT_DATE")
    Long countEntregasHoyPorArea(@Param("areaId") Integer areaId);

    /**
     * Obtener últimas N entregas.
     * Dashboard principal - actividad reciente.
     */
    @Query("SELECT e FROM EntregaEpp e ORDER BY e.fechaEntrega DESC")
    Page<EntregaEpp> findUltimasEntregas(Pageable pageable);

    /**
     * Obtener entregas por tipo.
     * Análisis de tipos de entrega (PROGRAMADA, URGENTE, etc.).
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.tipoEntrega = :tipoEntrega " +
            "ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findByTipoEntrega(@Param("tipoEntrega") EntregaEpp.TipoEntrega tipoEntrega);

    /**
     * Obtener entregas por estado.
     * Útil para filtrar entregas completadas, pendientes, etc.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.status = :status " +
            "ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findByStatus(@Param("status") String status);

    /**
     * Buscar entregas por DNI del trabajador.
     * Para búsquedas rápidas por DNI.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.trabajador.dni = :dni " +
            "ORDER BY e.fechaEntrega DESC")
    List<EntregaEpp> findByTrabajadorDni(@Param("dni") String dni);

    /**
     * Contar total de entregas de un trabajador.
     * Métricas del trabajador.
     */
    @Query("SELECT COUNT(e) FROM EntregaEpp e WHERE e.trabajador.trabajadorId = :trabajadorId")
    Long countByTrabajador(@Param("trabajadorId") Integer trabajadorId);

    /**
     * Obtener entregas de un área con paginación.
     * Vista del jefe de área para ver todas las entregas de su área.
     */
    @Query("SELECT e FROM EntregaEpp e WHERE e.trabajador.area.areaId = :areaId " +
            "ORDER BY e.fechaEntrega DESC")
    Page<EntregaEpp> findByArea(@Param("areaId") Integer areaId, Pageable pageable);
}