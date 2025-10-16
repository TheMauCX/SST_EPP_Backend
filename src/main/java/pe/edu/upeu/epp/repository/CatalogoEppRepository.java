package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoEpp;

import java.util.List;
import java.util.Optional;

// ==================== CATALOGO EPP REPOSITORY ====================
@Repository
public interface CatalogoEppRepository extends JpaRepository<CatalogoEpp, Integer>,
        JpaSpecificationExecutor<CatalogoEpp> {
    Optional<CatalogoEpp> findByCodigoIdentificacion(String codigoIdentificacion);
    List<CatalogoEpp> findByTipoUso(CatalogoEpp.TipoUso tipoUso);
    List<CatalogoEpp> findByActivoTrue();
    boolean existsByCodigoIdentificacion(String codigoIdentificacion);

    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.nombreEpp) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.activo = true")
    List<CatalogoEpp> buscarPorNombreActivo(@Param("nombre") String nombre);
    /**
     * Buscar EPPs por marca.
     */
    @Query("SELECT c FROM CatalogoEpp c WHERE LOWER(c.marca) LIKE LOWER(CONCAT('%', :marca, '%')) AND c.activo = true")
    List<CatalogoEpp> findByMarca(@Param("marca") String marca);

    /**
     * Listar todas las marcas distintas.
     */
    @Query("SELECT DISTINCT c.marca FROM CatalogoEpp c WHERE c.marca IS NOT NULL AND c.activo = true ORDER BY c.marca")
    List<String> findAllMarcasDistinct();

    /**
     * Buscar EPPs por unidad de medida.
     */
    @Query("SELECT c FROM CatalogoEpp c WHERE c.unidadMedida = :unidadMedida AND c.activo = true")
    List<CatalogoEpp> findByUnidadMedida(@Param("unidadMedida") String unidadMedida);

    /**
     * Listar todas las unidades de medida distintas.
     */
    @Query("SELECT DISTINCT c.unidadMedida FROM CatalogoEpp c WHERE c.unidadMedida IS NOT NULL AND c.activo = true ORDER BY c.unidadMedida")
    List<String> findAllUnidadesMedidaDistinct();
}