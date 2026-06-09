package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.AjusteInventarioDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralUpdateDTO;
import pe.edu.upeu.epp.dto.response.InventarioAgrupadoResponseDTO;
import pe.edu.upeu.epp.dto.response.InventarioCentralResponseDTO;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioCentralService {

    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final EstadoEppRepository estadoEppRepository;
    private final CatalogoTallaRepository tallaRepository;

    // ── Crear ────────────────────────────────────────────────────────────────

    @Transactional
    public InventarioCentralResponseDTO crear(InventarioCentralRequestDTO request) {
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + request.getEppId()));
        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado: " + request.getEstadoId()));

        CatalogoTalla talla = null;
        if (request.getTallaId() != null) {
            talla = tallaRepository.findById(request.getTallaId())
                    .orElseThrow(() -> new EntityNotFoundException("Talla no encontrada: " + request.getTallaId()));
        }

        boolean existe = talla != null
                ? inventarioCentralRepository.existsByEppAndLoteAndEstadoAndTalla(epp, request.getLote(), estado, talla)
                : inventarioCentralRepository.findByEppAndLoteAndEstadoSinTalla(epp, request.getLote(), estado).isPresent();
        if (existe) throw new BusinessException("Ya existe un registro con esa combinación (EPP, lote, estado, talla).");

        InventarioCentral inv = InventarioCentral.builder()
                .epp(epp).estado(estado).talla(talla)
                .cantidadActual(request.getCantidadActual())
                .ubicacionBodega(request.getUbicacionBodega())
                .lote(request.getLote())
                .fechaAdquisicion(request.getFechaAdquisicion())
                .costoUnitario(request.getCostoUnitario())
                .proveedor(request.getProveedor())
                .fechaVencimiento(request.getFechaVencimiento())
                .observaciones(request.getObservaciones())
                .fechaCreacion(LocalDateTime.now())
                .build();

        return mapToResponseDTO(inventarioCentralRepository.save(inv));
    }

    // ── Listar estándar ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InventarioCentralResponseDTO> listarTodos(Pageable pageable, String sort, String filter) {
        boolean ordenAlfa     = "alpha".equalsIgnoreCase(sort);
        boolean soloStockBajo = "low_stock".equalsIgnoreCase(filter);

        Pageable efectivo = ordenAlfa
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by("epp.nombreEpp").ascending())
                : pageable;

        if (soloStockBajo) return inventarioCentralRepository.findStockBajoPaginado(efectivo).map(this::mapToResponseDTO);
        if (ordenAlfa)     return inventarioCentralRepository.findAllOrderByNombreEppAsc(efectivo).map(this::mapToResponseDTO);
        return inventarioCentralRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    // ── Vista agrupada paginada ───────────────────────────────────────────────

    /**
     * Agrupa registros por (epp_id, proveedor, lote) y pagina el resultado.
     * Cada grupo expone todas las tallas con sus cantidades y costos.
     */
    @Transactional(readOnly = true)
    public Page<InventarioAgrupadoResponseDTO> listarAgrupadoPaginado(Pageable pageable) {
        List<InventarioCentral> todos = inventarioCentralRepository.findAll();

        Map<String, List<InventarioCentral>> grupos = todos.stream()
                .collect(Collectors.groupingBy(inv ->
                        inv.getEpp().getEppId()
                        + "||" + Objects.toString(inv.getProveedor(), "")
                        + "||" + Objects.toString(inv.getLote(), "")));

        List<InventarioAgrupadoResponseDTO> agrupados = grupos.values().stream()
                .map(registros -> {
                    InventarioCentral primero = registros.get(0);
                    CatalogoEpp epp = primero.getEpp();

                    int totalCantidad = registros.stream()
                            .mapToInt(r -> r.getCantidadActual() != null ? r.getCantidadActual() : 0).sum();

                    List<InventarioAgrupadoResponseDTO.DetalleTallaDTO> detalles = registros.stream()
                            .map(r -> InventarioAgrupadoResponseDTO.DetalleTallaDTO.builder()
                                    .inventarioId(r.getInventarioCentralId())
                                    .tallaId(r.getTalla() != null ? r.getTalla().getTallaId() : null)
                                    .tallaNombre(r.getTalla() != null ? r.getTalla().getNombre() : "Sin talla")
                                    .cantidadActual(r.getCantidadActual())
                                    .costoUnitario(r.getCostoUnitario())
                                    .estadoNombre(r.getEstado() != null ? r.getEstado().getNombre() : null)
                                    .build())
                            .sorted(Comparator.comparing(d -> d.getTallaNombre() != null ? d.getTallaNombre() : ""))
                            .collect(Collectors.toList());

                    boolean necesita = epp.getCantidadMinima() != null && totalCantidad <= epp.getCantidadMinima();

                    return InventarioAgrupadoResponseDTO.builder()
                            .eppId(epp.getEppId()).eppNombre(epp.getNombreEpp())
                            .tipoUso(epp.getTipoUso()).color(epp.getColor())
                            .lote(primero.getLote()).proveedor(primero.getProveedor())
                            .fechaAdquisicion(primero.getFechaAdquisicion())
                            .fechaVencimiento(primero.getFechaVencimiento())
                            .totalCantidad(totalCantidad)
                            .cantidadMinima(epp.getCantidadMinima())
                            .cantidadMaxima(epp.getCantidadMaxima())
                            .necesitaReposicion(necesita)
                            .detallesPorTalla(detalles)
                            .build();
                })
                .sorted(Comparator.comparing(InventarioAgrupadoResponseDTO::getEppNombre))
                .collect(Collectors.toList());

        // Paginación manual
        int total = agrupados.size();
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), total);
        List<InventarioAgrupadoResponseDTO> pageContent =
                start > total ? Collections.emptyList() : agrupados.subList(start, end);

        return new PageImpl<>(pageContent, pageable, total);
    }

    // ── Consultas estándar ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InventarioCentralResponseDTO obtenerPorId(Integer id) {
        return mapToResponseDTO(inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id)));
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarPorEpp(Integer eppId) {
        CatalogoEpp epp = catalogoEppRepository.findById(eppId)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado: " + eppId));
        return inventarioCentralRepository.findByEpp(epp).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarStockBajo() {
        return inventarioCentralRepository.findStockBajo().stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarProximosAVencer() {
        return inventarioCentralRepository.findByFechaVencimientoBefore(LocalDate.now().plusDays(30))
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public InventarioCentralResponseDTO actualizar(Integer id, InventarioCentralUpdateDTO request) {
        InventarioCentral inv = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
        if (request.getEstadoId() != null)
            inv.setEstado(estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado")));
        if (request.getLote()            != null) inv.setLote(request.getLote());
        if (request.getUbicacionBodega() != null) inv.setUbicacionBodega(request.getUbicacionBodega());
        if (request.getObservaciones()   != null) inv.setObservaciones(request.getObservaciones());
        return mapToResponseDTO(inventarioCentralRepository.save(inv));
    }

    @Transactional
    public InventarioCentralResponseDTO ajustarStock(Integer id, AjusteInventarioDTO request) {
        InventarioCentral inv = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
        int ajuste = "SALIDA".equals(request.getTipoAjuste())
                ? -request.getCantidadAjuste() : request.getCantidadAjuste();
        int nueva = inv.getCantidadActual() + ajuste;
        if (nueva < 0) throw new BusinessException("El ajuste resultaría en cantidad negativa.");
        inv.setCantidadActual(nueva);
        inv.setObservaciones(String.format("[AJUSTE %s] %d uds. Motivo: %s",
                request.getTipoAjuste(), Math.abs(ajuste), request.getMotivo()));
        return mapToResponseDTO(inventarioCentralRepository.save(inv));
    }

    @Transactional
    public void eliminar(Integer id) {
        InventarioCentral inv = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
        if (inv.getCantidadActual() > 0)
            throw new BusinessException("No se puede eliminar inventario con stock disponible.");
        inventarioCentralRepository.delete(inv);
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    private InventarioCentralResponseDTO mapToResponseDTO(InventarioCentral inv) {
        CatalogoEpp epp = inv.getEpp();
        return InventarioCentralResponseDTO.builder()
                .inventarioId(inv.getInventarioCentralId())
                .eppId(epp.getEppId()).eppNombre(epp.getNombreEpp()).tipoUso(epp.getTipoUso())
                .tallaId(inv.getTalla() != null ? inv.getTalla().getTallaId() : null)
                .tallaNombre(inv.getTalla() != null ? inv.getTalla().getNombre() : null)
                .estadoId(inv.getEstado().getEstadoId()).estadoNombre(inv.getEstado().getNombre())
                .estadoPermiteUso(inv.getEstado().getPermiteUso()).estadoColorHex(inv.getEstado().getColorHex())
                .cantidadActual(inv.getCantidadActual())
                .cantidadMinima(epp.getCantidadMinima()).cantidadMaxima(epp.getCantidadMaxima())
                .ubicacionBodega(inv.getUbicacionBodega()).lote(inv.getLote())
                .fechaAdquisicion(inv.getFechaAdquisicion()).costoUnitario(inv.getCostoUnitario())
                .proveedor(inv.getProveedor()).fechaVencimiento(inv.getFechaVencimiento())
                .observaciones(inv.getObservaciones()).ultimaActualizacion(inv.getUltimaActualizacion())
                .necesitaReposicion(inv.necesitaReposicion())
                .diasParaVencer(inv.getFechaVencimiento() != null
                        ? (int) ChronoUnit.DAYS.between(LocalDate.now(), inv.getFechaVencimiento()) : null)
                .build();
    }
}
