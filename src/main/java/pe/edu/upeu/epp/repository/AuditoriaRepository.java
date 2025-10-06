package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Auditoria;
import pe.edu.upeu.epp.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    List<Auditoria> findByTablaAfectada(String tablaAfectada);
    List<Auditoria> findByUsuario(Usuario usuario);
    List<Auditoria> findByOperacion(Auditoria.TipoOperacion operacion);

    @Query("SELECT a FROM Auditoria a WHERE a.fechaOperacion BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fechaOperacion DESC")
    List<Auditoria> findByFechaRango(@Param("fechaInicio") LocalDateTime fechaInicio,
                                     @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT a FROM Auditoria a WHERE a.tablaAfectada = :tabla AND a.registroId = :registroId ORDER BY a.fechaOperacion DESC")
    List<Auditoria> findHistorialPorRegistro(@Param("tabla") String tabla,
                                             @Param("registroId") Integer registroId);
}
