package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Trabajador;
import pe.edu.upeu.epp.entity.Usuario;

import java.util.List;
import java.util.Optional;

// ==================== USUARIO REPOSITORY ====================
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByTrabajador(Trabajador trabajador);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.roles " +
            "WHERE u.nombreUsuario = :nombreUsuario " +
            "AND u.activo = true")
    Optional<Usuario> findByNombreUsuarioWithRoles(@Param("nombreUsuario") String nombreUsuario);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombreRol = :nombreRol AND u.activo = true")
    List<Usuario> findByRoles_NombreRol(@Param("nombreRol") String nombreRol);

    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByEmail(String email);
}