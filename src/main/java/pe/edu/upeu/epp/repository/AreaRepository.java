package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Area;

import java.util.List;
import java.util.Optional;

// ==================== AREA REPOSITORY ====================
@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
    Optional<Area> findByCodigoArea(String codigoArea);
    List<Area> findByActivoTrue();
    boolean existsByNombreArea(String nombreArea);
    boolean existsByCodigoArea(String codigoArea);
}