package pe.edu.upeu.epp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.InventarioArea;

import java.util.List;
import java.util.Optional;

// ==================== INVENTARIO AREA REPOSITORY ====================
@Repository
public interface InventarioAreaRepository extends JpaRepository<InventarioArea, Integer> {
    Optional<InventarioArea> findByEppAndArea(CatalogoEpp epp, Area area);
    List<InventarioArea> findByArea(Area area);

    @Query("SELECT ia FROM InventarioArea ia WHERE ia.epp.eppId = :eppId AND ia.area.areaId = :areaId")
    Optional<InventarioArea> findByEppIdAndAreaId(@Param("eppId") Integer eppId,
                                                  @Param("areaId") Integer areaId);

    @Query("SELECT ia FROM InventarioArea ia WHERE ia.area.areaId = :areaId")
    List<InventarioArea> findByAreaId(@Param("areaId") Integer areaId);

    @Query("SELECT ia FROM InventarioArea ia WHERE ia.area.areaId = :areaId AND ia.cantidadActual <= ia.cantidadMinima")
    List<InventarioArea> findStockBajoPorArea(@Param("areaId") Integer areaId);

    @Query("SELECT ia FROM InventarioArea ia WHERE ia.cantidadActual <= ia.cantidadMinima")
    List<InventarioArea> findAllStockCritico();
}