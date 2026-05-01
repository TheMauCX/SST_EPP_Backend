package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.InventarioAreaRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioAreaUpdateDTO;
import pe.edu.upeu.epp.dto.request.TransferenciaStockDTO;
import pe.edu.upeu.epp.dto.response.InventarioAreaResponseDTO;
import pe.edu.upeu.epp.entity.Area;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InventarioArea;
import pe.edu.upeu.epp.entity.InventarioCentral;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.AreaRepository;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.EstadoEppRepository;
import pe.edu.upeu.epp.repository.InventarioAreaRepository;
import pe.edu.upeu.epp.repository.InventarioCentralRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioAreaService {

    private final InventarioAreaRepository inventarioAreaRepository;
    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final AreaRepository areaRepository;
    private final EstadoEppRepository estadoEppRepository;

    @Transactional
    public InventarioAreaResponseDTO crear(InventarioAreaRequestDTO request) {
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada con ID: " + request.getAreaId()));

        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        boolean existe = inventarioAreaRepository.existsByEppAndAreaAndEstado(epp, area, estado);
        if (existe) {
            throw new BusinessException("Ya existe inventario para este EPP, Área y Estado.");
        }

        InventarioArea inventario = InventarioArea.builder()
                .epp(epp).area(area).estado(estado)
                .cantidadActual(request.getCantidadActual())
                .cantidadMinima(request.getCantidadMinima())
                .cantidadMaxima(request.getCantidadMaxima())
                .ubicacion(request.getUbicacion())
                .build();

        return mapToResponseDTO(inventarioAreaRepository.save(inventario));
    }

    @Transactional
    public InventarioAreaResponseDTO actualizar(Integer id, InventarioAreaUpdateDTO request) {
        InventarioArea inventarioActual = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        EstadoEpp nuevoEstado = inventarioActual.getEstado();
        if (request.getEstadoId() != null) {
            nuevoEstado = estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado"));
        }

        if (!nuevoEstado.getEstadoId().equals(inventarioActual.getEstado().getEstadoId())) {
            Optional<InventarioArea> registroExistente = inventarioAreaRepository.findByEppAndAreaAndEstado(
                    inventarioActual.getEpp(), inventarioActual.getArea(), nuevoEstado);

            if (registroExistente.isPresent() && !registroExistente.get().getInventarioAreaId().equals(id)) {
                InventarioArea destino = registroExistente.get();
                destino.setCantidadActual(destino.getCantidadActual() + inventarioActual.getCantidadActual());

                if(request.getCantidadMinima() != null) destino.setCantidadMinima(request.getCantidadMinima());
                if(request.getCantidadMaxima() != null) destino.setCantidadMaxima(request.getCantidadMaxima());
                if(request.getUbicacion() != null) destino.setUbicacion(request.getUbicacion());

                InventarioArea consolidado = inventarioAreaRepository.save(destino);
                inventarioAreaRepository.delete(inventarioActual);
                return mapToResponseDTO(consolidado);
            }
        }

        if (request.getEstadoId() != null) inventarioActual.setEstado(nuevoEstado);
        if (request.getCantidadMinima() != null) inventarioActual.setCantidadMinima(request.getCantidadMinima());
        if (request.getCantidadMaxima() != null) inventarioActual.setCantidadMaxima(request.getCantidadMaxima());
        if (request.getUbicacion() != null) inventarioActual.setUbicacion(request.getUbicacion());

        return mapToResponseDTO(inventarioAreaRepository.save(inventarioActual));
    }

    @Transactional(readOnly = true)
    public InventarioAreaResponseDTO obtenerPorId(Integer id) {
        InventarioArea inventario = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));
        return mapToResponseDTO(inventario);
    }

    @Transactional(readOnly = true)
    public Page<InventarioAreaResponseDTO> listarTodos(Pageable pageable) {
        return inventarioAreaRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarPorArea(Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada"));
        return inventarioAreaRepository.findByArea(area).stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoPorArea(Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada"));
        return inventarioAreaRepository.findStockBajoByArea(area).stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoGlobal() {
        return inventarioAreaRepository.findStockBajo().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public InventarioAreaResponseDTO transferirStockCentralAArea(TransferenciaStockDTO request) {
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado"));
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada"));

        List<InventarioCentral> inventariosCentrales = inventarioCentralRepository.findByEppAndEstadoPermiteUsoOrderByCantidadDesc(epp, true);

        InventarioCentral invCentral = inventariosCentrales.stream()
                .filter(inv -> inv.getCantidadDisponible() >= request.getCantidad())
                .findFirst()
                .orElseThrow(() -> new BusinessException("No hay stock suficiente en inventario central"));

        invCentral.setCantidadActual(invCentral.getCantidadActual() - request.getCantidad());
        inventarioCentralRepository.save(invCentral);

        EstadoEpp estado = invCentral.getEstado();
        Optional<InventarioArea> invAreaOpt = inventarioAreaRepository.findByEppAndAreaAndEstado(epp, area, estado);

        InventarioArea invArea;
        if (invAreaOpt.isPresent()) {
            invArea = invAreaOpt.get();
            invArea.setCantidadActual(invArea.getCantidadActual() + request.getCantidad());
        } else {
            invArea = InventarioArea.builder()
                    .epp(epp).area(area).estado(estado)
                    .cantidadActual(request.getCantidad())
                    .cantidadMinima(5)
                    .cantidadMaxima(request.getCantidad() * 2)
                    .ubicacion("Almacen " + area.getNombreArea())
                    .build();
        }

        return mapToResponseDTO(inventarioAreaRepository.save(invArea));
    }

    @Transactional
    public void eliminar(Integer id) {
        InventarioArea inventario = inventarioAreaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No encontrado"));
        if (inventario.getCantidadActual() > 0) throw new BusinessException("No se puede eliminar con stock disponible");
        inventarioAreaRepository.delete(inventario);
    }

    private InventarioAreaResponseDTO mapToResponseDTO(InventarioArea inventario) {
        return InventarioAreaResponseDTO.builder()
                .inventarioAreaId(inventario.getInventarioAreaId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(null) // Campo eliminado de CatalogoEpp
                .tipoUso(inventario.getEpp().getTipoUso())
                .areaId(inventario.getArea().getAreaId())
                .areaNombre(inventario.getArea().getNombreArea())
                .estadoId(inventario.getEstado().getEstadoId())
                .estadoNombre(inventario.getEstado().getNombre())
                .estadoDescripcion(inventario.getEstado().getDescripcion())
                .estadoPermiteUso(inventario.getEstado().getPermiteUso())
                .estadoColorHex(inventario.getEstado().getColorHex())
                .cantidadActual(inventario.getCantidadActual())
                .cantidadMinima(inventario.getCantidadMinima())
                .cantidadMaxima(inventario.getCantidadMaxima())
                .ubicacion(inventario.getUbicacion())
                .ultimaActualizacion(inventario.getUltimaActualizacion())
                .necesitaReposicion(inventario.necesitaReposicion())
                .porcentajeStock(inventario.calcularPorcentajeStock())
                .build();
    }
}