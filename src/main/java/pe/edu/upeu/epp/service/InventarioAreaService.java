package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.InventarioAreaRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioAreaUpdateDTO;
import pe.edu.upeu.epp.dto.request.TransferenciaStockDTO;
import pe.edu.upeu.epp.dto.response.InventarioAreaResponseDTO;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

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
    private final CatalogoTallaRepository tallaRepository;

    // ── Transferencia atómica Central → Área ─────────────────────────────────

    @Transactional
    public InventarioAreaResponseDTO transferirStockCentralAArea(TransferenciaStockDTO request) {
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + request.getEppId()));
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada: " + request.getAreaId()));

        CatalogoTalla talla = null;
        if (request.getTallaId() != null) {
            talla = tallaRepository.findById(request.getTallaId())
                    .orElseThrow(() -> new EntityNotFoundException("Talla no encontrada: " + request.getTallaId()));
        }
        final CatalogoTalla tallaFinal = talla;

        InventarioCentral invCentral = buscarLoteCentral(epp, tallaFinal, request.getCantidad());

        int nuevaCantidadCentral = invCentral.getCantidadActual() - request.getCantidad();
        if (nuevaCantidadCentral < 0)
            throw new BusinessException("Transferencia rechazada: resultaría en cantidad negativa.");

        EstadoEpp estado = invCentral.getEstado();

        Optional<InventarioArea> invAreaOpt = tallaFinal != null
                ? inventarioAreaRepository.findByEppAndAreaAndEstadoAndTalla(epp, area, estado, tallaFinal)
                : inventarioAreaRepository.findByEppAndAreaAndEstadoSinTalla(epp, area, estado);

        InventarioArea invArea = invAreaOpt.map(ia -> {
            ia.setCantidadActual(ia.getCantidadActual() + request.getCantidad());
            return ia;
        }).orElseGet(() -> InventarioArea.builder()
                .epp(epp).area(area).estado(estado).talla(tallaFinal)
                .cantidadActual(request.getCantidad())
                .ubicacion("Almacén " + area.getNombreArea())
                .build());

        invCentral.setCantidadActual(nuevaCantidadCentral);
        inventarioCentralRepository.save(invCentral);
        InventarioArea guardada = inventarioAreaRepository.save(invArea);

        log.info("Transferencia OK. Central restante: {}, Área: {}",
                nuevaCantidadCentral, guardada.getCantidadActual());
        return mapToResponseDTO(guardada);
    }

    private InventarioCentral buscarLoteCentral(CatalogoEpp epp, CatalogoTalla talla, int cantidad) {
        return inventarioCentralRepository
                .findByEppAndEstadoPermiteUsoOrderByCantidadDesc(epp, true)
                .stream()
                .filter(inv -> talla != null ? talla.equals(inv.getTalla()) : inv.getTalla() == null)
                .filter(inv -> inv.getCantidadDisponible() >= cantidad)
                .findFirst()
                .orElseThrow(() -> new BusinessException(String.format(
                        "Stock insuficiente en central para '%s'%s. Solicitado: %d.",
                        epp.getNombreEpp(),
                        talla != null ? " talla " + talla.getNombre() : "",
                        cantidad)));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Transactional
    public InventarioAreaResponseDTO crear(InventarioAreaRequestDTO request) {
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + request.getEppId()));
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada: " + request.getAreaId()));
        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado: " + request.getEstadoId()));

        if (inventarioAreaRepository.existsByEppAndAreaAndEstado(epp, area, estado))
            throw new BusinessException("Ya existe inventario para este EPP, Área y Estado.");

        InventarioArea inv = InventarioArea.builder()
                .epp(epp).area(area).estado(estado)
                .cantidadActual(request.getCantidadActual())
                .ubicacion(request.getUbicacion())
                .build();
        return mapToResponseDTO(inventarioAreaRepository.save(inv));
    }

    @Transactional
    public InventarioAreaResponseDTO actualizar(Integer id, InventarioAreaUpdateDTO request) {
        InventarioArea inv = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
        if (request.getEstadoId() != null) {
            inv.setEstado(estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado")));
        }
        if (request.getUbicacion() != null) inv.setUbicacion(request.getUbicacion());
        return mapToResponseDTO(inventarioAreaRepository.save(inv));
    }

    @Transactional(readOnly = true)
    public InventarioAreaResponseDTO obtenerPorId(Integer id) {
        return mapToResponseDTO(inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id)));
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarPorArea(Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada: " + areaId));
        return inventarioAreaRepository.findByArea(area).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoPorArea(Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada: " + areaId));
        return inventarioAreaRepository.findStockBajoByArea(area).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioAreaResponseDTO> listarStockBajoGlobal() {
        return inventarioAreaRepository.findStockBajo().stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Integer id) {
        InventarioArea inv = inventarioAreaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
        if (inv.getCantidadActual() > 0)
            throw new BusinessException("No se puede eliminar inventario con stock disponible.");
        inventarioAreaRepository.delete(inv);
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    private InventarioAreaResponseDTO mapToResponseDTO(InventarioArea inv) {
        CatalogoEpp epp = inv.getEpp();
        return InventarioAreaResponseDTO.builder()
                .inventarioAreaId(inv.getInventarioAreaId())
                .eppId(epp.getEppId())
                .eppNombre(epp.getNombreEpp())
                .tipoUso(epp.getTipoUso())
                .areaId(inv.getArea().getAreaId())
                .areaNombre(inv.getArea().getNombreArea())
                .tallaId(inv.getTalla() != null ? inv.getTalla().getTallaId() : null)
                .tallaNombre(inv.getTalla() != null ? inv.getTalla().getNombre() : null)
                .estadoId(inv.getEstado().getEstadoId())
                .estadoNombre(inv.getEstado().getNombre())
                .estadoPermiteUso(inv.getEstado().getPermiteUso())
                .estadoColorHex(inv.getEstado().getColorHex())
                .cantidadActual(inv.getCantidadActual())
                .cantidadMinima(epp.getCantidadMinima())
                .cantidadMaxima(epp.getCantidadMaxima())
                .ubicacion(inv.getUbicacion())
                .ultimaActualizacion(inv.getUltimaActualizacion())
                .necesitaReposicion(inv.necesitaReposicion())
                .porcentajeStock(inv.calcularPorcentajeStock())
                .build();
    }
}
