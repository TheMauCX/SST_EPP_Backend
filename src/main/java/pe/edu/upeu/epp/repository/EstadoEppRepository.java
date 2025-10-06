package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.EstadoEpp;

import java.util.List;
import java.util.Optional;

// ==================== ESTADO EPP REPOSITORY ====================
@Repository
public interface EstadoEppRepository extends JpaRepository<EstadoEpp, Integer> {
    Optional<EstadoEpp> findByNombre(String nombre);
    List<EstadoEpp> findByPermiteUsoTrue();
}