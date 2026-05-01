package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.upeu.epp.entity.Compra;

public interface CompraRepository extends JpaRepository<Compra, Integer> {
}
