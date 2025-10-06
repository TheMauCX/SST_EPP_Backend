package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.InventarioCentral;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// ==================== INVENTARIO CENTRAL REPOSITORY ====================
@Repository
public interface InventarioCentralRepository extends JpaRepository<InventarioCentral, Integer> {
    Optional<InventarioCentral> findByEpp(CatalogoEpp epp);
    Optional<InventarioCentral> findByEppAndLote(CatalogoEpp epp, String lote);

    @Query("SELECT ic FROM InventarioCentral ic WHERE ic.epp.eppId = :eppId")
    Optional<InventarioCentral> findByEppId(@Param("eppId") Integer eppId);

    @Query("SELECT ic FROM InventarioCentral ic WHERE ic.cantidadActual <= ic.cantidadMinima")
    List<InventarioCentral> findStockBajo();

    @Query("SELECT ic FROM InventarioCentral ic WHERE ic.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin")
    List<InventarioCentral> findProximosAVencer(@Param("fechaInicio") LocalDate fechaInicio,
                                                @Param("fechaFin") LocalDate fechaFin);
}