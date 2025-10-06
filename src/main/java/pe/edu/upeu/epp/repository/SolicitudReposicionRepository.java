package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.SolicitudReposicion;
import pe.edu.upeu.epp.entity.Trabajador;

import java.util.List;
import java.util.Optional;

// ==================== SOLICITUD REPOSICION REPOSITORY ====================
@Repository
public interface SolicitudReposicionRepository extends JpaRepository<SolicitudReposicion, Integer>,
        JpaSpecificationExecutor<SolicitudReposicion> {
    List<SolicitudReposicion> findByEstadoSolicitud(SolicitudReposicion.EstadoSolicitud estado);
    List<SolicitudReposicion> findByArea(Area area);
    List<SolicitudReposicion> findBySolicitante(Trabajador solicitante);

    @Query("SELECT sr FROM SolicitudReposicion sr WHERE sr.area.areaId = :areaId AND sr.estadoSolicitud = :estado ORDER BY sr.prioridad DESC, sr.fechaSolicitud ASC")
    List<SolicitudReposicion> findByAreaIdAndEstado(@Param("areaId") Integer areaId,
                                                    @Param("estado") SolicitudReposicion.EstadoSolicitud estado);

    @Query("SELECT sr FROM SolicitudReposicion sr WHERE sr.estadoSolicitud = :estado ORDER BY sr.prioridad DESC, sr.fechaSolicitud ASC")
    List<SolicitudReposicion> findByEstadoOrderByPrioridadAndFecha(@Param("estado") SolicitudReposicion.EstadoSolicitud estado);

    @Query("SELECT COUNT(sr) FROM SolicitudReposicion sr WHERE sr.estadoSolicitud = 'PENDIENTE' AND sr.prioridad IN ('ALTA', 'URGENTE')")
    long countSolicitudesUrgentes();

    @Query("SELECT sr FROM SolicitudReposicion sr WHERE sr.area.areaId IN :areaIds AND sr.estadoSolicitud IN :estados")
    List<SolicitudReposicion> findByAreasAndEstados(@Param("areaIds") List<Integer> areaIds,
                                                    @Param("estados") List<SolicitudReposicion.EstadoSolicitud> estados);
}