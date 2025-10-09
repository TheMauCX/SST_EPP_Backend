package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.Trabajador;

import java.util.List;
import java.util.Optional;

// ==================== TRABAJADOR REPOSITORY ====================
@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Integer> {
    Optional<Trabajador> findByDni(String dni);

    Optional<Trabajador> findByCodigoQrPhotocheck(String codigoQrPhotocheck);

    List<Trabajador> findByArea(Area area);

    List<Trabajador> findByAreaAndEstado(Area area, Trabajador.EstadoTrabajador estado);

    long countByAreaAndEstado(Area area, Trabajador.EstadoTrabajador estado);

    @Query("SELECT t FROM Trabajador t WHERE t.area.areaId = :areaId AND t.estado = 'ACTIVO' ORDER BY t.apellidos, t.nombres")
    List<Trabajador> findTrabajadoresActivosPorArea(@Param("areaId") Integer areaId);

    @Query("SELECT t FROM Trabajador t WHERE " +
            "LOWER(CONCAT(t.nombres, ' ', t.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
            "ORDER BY t.apellidos, t.nombres")
    List<Trabajador> buscarPorNombre(@Param("nombre") String nombre);
}