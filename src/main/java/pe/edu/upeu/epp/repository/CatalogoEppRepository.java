package pe.edu.upeu.epp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoEpp;

import java.util.List;

@Repository
public interface CatalogoEppRepository extends JpaRepository<CatalogoEpp, Integer>,
        JpaSpecificationExecutor<CatalogoEpp> {

    List<CatalogoEpp> findByTipoUso(CatalogoEpp.TipoUso tipoUso);

    List<CatalogoEpp> findByActivoTrue();

    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.nombreEpp) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.activo = true")
    List<CatalogoEpp> buscarPorNombreActivo(@Param("nombre") String nombre);

    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.fabricante) LIKE LOWER(CONCAT('%', :fabricante, '%')) AND c.activo = true")
    List<CatalogoEpp> findByFabricante(@Param("fabricante") String fabricante);

    @Query("SELECT DISTINCT c.fabricante FROM CatalogoEpp c WHERE c.fabricante IS NOT NULL AND c.activo = true ORDER BY c.fabricante")
    List<String> findAllFabricantesDistinct();

    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.aprobacionesNormas) LIKE LOWER(CONCAT('%', :norma, '%')) AND c.activo = true")
    List<CatalogoEpp> buscarPorNorma(@Param("norma") String norma);

    // ── HU-23: Filtros por rotación ──────────────────────────────────────

    /**
     * EPPs con al menos una entrega en los últimos 90 días (alta rotación).
     */
    @Query("SELECT DISTINCT c FROM CatalogoEpp c " +
           "JOIN DetalleEntregaEpp d ON d.epp = c " +
           "JOIN d.entrega e " +
           "WHERE c.activo = true " +
           "AND e.fechaEntrega >= :desde")
    Page<CatalogoEpp> findConRotacionAlta(
            @Param("desde") java.time.LocalDateTime desde,
            Pageable pageable);

    /**
     * EPPs sin ninguna entrega en los últimos 90 días (inmovilizados/baja rotación).
     */
    @Query("SELECT c FROM CatalogoEpp c " +
           "WHERE c.activo = true " +
           "AND c.eppId NOT IN (" +
           "  SELECT DISTINCT d.epp.eppId FROM DetalleEntregaEpp d " +
           "  WHERE d.entrega.fechaEntrega >= :desde" +
           ")")
    Page<CatalogoEpp> findSinRotacion(
            @Param("desde") java.time.LocalDateTime desde,
            Pageable pageable);
}
