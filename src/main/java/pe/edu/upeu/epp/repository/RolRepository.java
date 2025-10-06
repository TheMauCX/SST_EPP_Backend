package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// ==================== ROL REPOSITORY ====================
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreRol(String nombreRol);
    boolean existsByNombreRol(String nombreRol);
}