package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoEpp;

import java.util.List;

// ==================== CATALOGO EPP REPOSITORY ====================
@Repository
public interface CatalogoEppRepository extends JpaRepository<CatalogoEpp, Integer>,
        JpaSpecificationExecutor<CatalogoEpp> {

    List<CatalogoEpp> findByTipoUso(CatalogoEpp.TipoUso tipoUso);

    List<CatalogoEpp> findByActivoTrue();

    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.nombreEpp) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.activo = true")
    List<CatalogoEpp> buscarPorNombreActivo(@Param("nombre") String nombre);

    /**
     * Buscar EPPs por fabricante (Reemplaza a la antigua búsqueda por marca).
     */
    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.fabricante) LIKE LOWER(CONCAT('%', :fabricante, '%')) AND c.activo = true")
    List<CatalogoEpp> findByFabricante(@Param("fabricante") String fabricante);

    /**
     * Listar todos los fabricantes distintos para usar en filtros de UI.
     */
    @Query("SELECT DISTINCT c.fabricante FROM CatalogoEpp c WHERE c.fabricante IS NOT NULL AND c.activo = true ORDER BY c.fabricante")
    List<String> findAllFabricantesDistinct();

    /**
     * Buscar EPPs por Norma o Aprobación (ej. "ANSI", "ISO").
     */
    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.aprobacionesNormas) LIKE LOWER(CONCAT('%', :norma, '%')) AND c.activo = true")
    List<CatalogoEpp> buscarPorNorma(@Param("norma") String norma);
}