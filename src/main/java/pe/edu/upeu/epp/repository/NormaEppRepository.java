package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.NormaEpp;
import pe.edu.upeu.epp.entity.NormaEpp.Categoria;
import pe.edu.upeu.epp.entity.NormaEpp.Organismo;

import java.util.List;
import java.util.Optional;

@Repository
public interface NormaEppRepository extends JpaRepository<NormaEpp, Integer> {

    Optional<NormaEpp> findByCodigo(String codigo);

    List<NormaEpp> findByActivoTrue();

    List<NormaEpp> findByOrganismoAndActivoTrue(Organismo organismo);

    List<NormaEpp> findByCategoriaAndActivoTrue(Categoria categoria);

    boolean existsByCodigo(String codigo);
}
