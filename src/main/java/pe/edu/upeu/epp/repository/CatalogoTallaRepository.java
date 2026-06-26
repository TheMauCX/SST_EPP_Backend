package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoTalla;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoTallaRepository extends JpaRepository<CatalogoTalla, Integer> {

    Optional<CatalogoTalla> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndTallaIdNot(String nombre, Integer tallaId);

    List<CatalogoTalla> findAllByOrderByOrdenVisualizacionAscNombreAsc();
}
