package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.AjusteInventarioDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralUpdateDTO;
import pe.edu.upeu.epp.dto.response.InventarioCentralResponseDTO;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioCentralService {

    private final InventarioCentralRepository inventarioCentralRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final EstadoEppRepository estadoEppRepository;
    private final CatalogoTallaRepository tallaRepository;

    // ── Crear ────────────────────────────────────────────────────────────

    @Transactional
    public InventarioCentralResponseDTO crear(InventarioCentralRequestDTO request) {
        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));
        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        // Resolver talla (opcional)
        CatalogoTalla talla = null;
        if (request.getTallaId() != null) {
            talla = tallaRepository.findById(request.getTallaId())
                    .orElseThrow(() -> new EntityNotFoundException("Talla no encontrada con ID: " + request.getTallaId()));
        }

        // Verificar unicidad con la nueva clave (epp, lote, estado, talla)
        boolean existe = talla != null
                ? inventarioCentralRepository.existsByEppAndLoteAndEstadoAndTalla(epp, request.getLote(), estado, talla)
                : inventarioCentralRepository.findByEppAndLoteAndEstadoSinTalla(epp, request.getLote(), estado).isPresent();

        if (existe) {
            throw new BusinessException(String.format(
                    "Ya existe un registro para EPP '%s', Lote '%s', Estado '%s'%s",
                    epp.getNombreEpp(), request.getLote(), estado.getNombre(),
                    talla != null ? ", Talla '" + talla.getNombre() + "'" : " (sin talla)"));
        }

        InventarioCentral inventario = InventarioCentral.builder()
                .epp(epp).estado(estado).talla(talla)
                .cantidadActual(request.getCantidadActual())
                .cantidadMinima(request.getCantidadMinima())
                .cantidadMaxima(request.getCantidadMaxima())
                .ubicacionBodega(request.getUbicacionBodega())
                .lote(request.getLote())
                .fechaAdquisicion(request.getFechaAdquisicion())
                .costoUnitario(request.getCostoUnitario())
                .proveedor(request.getProveedor())
                .fechaVencimiento(request.getFechaVencimiento())
                .observaciones(request.getObservaciones())
                .fechaCreacion(LocalDateTime.now())
                .build();

        return mapToResponseDTO(inventarioCentralRepository.save(inventario));
    }

    // ── Listar con filtros HU-24 ─────────────────────────────────────────

    /**
     * Listar inventario central con filtros opcionales:
     *   sort=alpha       → ordenar alfabéticamente por nombre de EPP
     *   filter=low_stock → solo EPPs con stock ≤ mínimo
     *
     * Los filtros son combinables.
     */
    @Transactional(readOnly = true)
    public Page<InventarioCentralResponseDTO> listarTodos(Pageable pageable, String sort, String filter) {
        boolean ordenAlfa    = "alpha".equalsIgnoreCase(sort);
        boolean soloStockBajo = "low_stock".equalsIgnoreCase(filter);

        // Construir Pageable con sort correcto
        Pageable pageableEfectivo = pageable;
        if (ordenAlfa) {
            pageableEfectivo = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("epp.nombreEpp").ascending());
        }

        if (soloStockBajo && ordenAlfa) {
            return inventarioCentralRepository.findStockBajoPaginado(pageableEfectivo)
                    .map(this::mapToResponseDTO);
        } else if (soloStockBajo) {
            return inventarioCentralRepository.findStockBajoPaginado(pageable)
                    .map(this::mapToResponseDTO);
        } else if (ordenAlfa) {
            return inventarioCentralRepository.findAllOrderByNombreEppAsc(pageableEfectivo)
                    .map(this::mapToResponseDTO);
        } else {
            return inventarioCentralRepository.findAll(pageable).map(this::mapToResponseDTO);
        }
    }

    @Transactional(readOnly = true)
    public Page<InventarioCentralResponseDTO> listarTodos(Pageable pageable) {
        return listarTodos(pageable, null, null);
    }

    // ── Actualizar ───────────────────────────────────────────────────────

    @Transactional
    public InventarioCentralResponseDTO actualizar(Integer id, InventarioCentralUpdateDTO request) {
        InventarioCentral inventarioActual = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        String nuevoLote = request.getLote() != null ? request.getLote() : inventarioActual.getLote();
        EstadoEpp nuevoEstado = inventarioActual.getEstado();
        if (request.getEstadoId() != null) {
            nuevoEstado = estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado: " + request.getEstadoId()));
        }

        boolean hayCambiosClave = !nuevoLote.equals(inventarioActual.getLote())
                || !nuevoEstado.getEstadoId().equals(inventarioActual.getEstado().getEstadoId());

        if (hayCambiosClave) {
            CatalogoTalla tallaActual = inventarioActual.getTalla();
            Optional<InventarioCentral> registroExistente = tallaActual != null
                    ? inventarioCentralRepository.findByEppAndLoteAndEstadoAndTalla(
                            inventarioActual.getEpp(), nuevoLote, nuevoEstado, tallaActual)
                    : inventarioCentralRepository.findByEppAndLoteAndEstadoSinTalla(
                            inventarioActual.getEpp(), nuevoLote, nuevoEstado);

            if (registroExistente.isPresent() && !registroExistente.get().getInventarioCentralId().equals(id)) {
                InventarioCentral destino = registroExistente.get();
                destino.setCantidadActual(destino.getCantidadActual() + inventarioActual.getCantidadActual());
                if (request.getCantidadMinima() != null) destino.setCantidadMinima(request.getCantidadMinima());
                if (request.getCantidadMaxima() != null) destino.setCantidadMaxima(request.getCantidadMaxima());
                if (request.getUbicacionBodega() != null) destino.setUbicacionBodega(request.getUbicacionBodega());
                InventarioCentral consolidado = inventarioCentralRepository.save(destino);
                inventarioCentralRepository.delete(inventarioActual);
                return mapToResponseDTO(consolidado);
            }
        }

        if (request.getEstadoId() != null) inventarioActual.setEstado(nuevoEstado);
        if (request.getLote() != null) inventarioActual.setLote(nuevoLote);
        if (request.getCantidadMinima() != null) inventarioActual.setCantidadMinima(request.getCantidadMinima());
        if (request.getCantidadMaxima() != null) inventarioActual.setCantidadMaxima(request.getCantidadMaxima());
        if (request.getUbicacionBodega() != null) inventarioActual.setUbicacionBodega(request.getUbicacionBodega());
        if (request.getObservaciones() != null) inventarioActual.setObservaciones(request.getObservaciones());

        return mapToResponseDTO(inventarioCentralRepository.save(inventarioActual));
    }

    // ── Consultas básicas ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InventarioCentralResponseDTO obtenerPorId(Integer id) {
        return mapToResponseDTO(inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id)));
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarPorEpp(Integer eppId) {
        CatalogoEpp epp = catalogoEppRepository.findById(eppId)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + eppId));
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
    public InventarioCentralResponseDTO ajustarStock(Integer id, AjusteInventarioDTO request) {
        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        int ajuste = "SALIDA".equals(request.getTipoAjuste())
                ? -request.getCantidadAjuste()
                : request.getCantidadAjuste();

        int nuevaCantidad = inventario.getCantidadActual() + ajuste;
        if (nuevaCantidad < 0) throw new BusinessException("El ajuste resultaría en cantidad negativa.");

        inventario.setCantidadActual(nuevaCantidad);
        inventario.setObservaciones(String.format("[AJUSTE %s] %d unidades. Motivo: %s",
                request.getTipoAjuste(), Math.abs(ajuste), request.getMotivo()));
        return mapToResponseDTO(inventarioCentralRepository.save(inventario));
    }

    @Transactional
    public void eliminar(Integer id) {
        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));
        if (inventario.getCantidadActual() > 0)
            throw new BusinessException("No se puede eliminar inventario con stock disponible.");
        inventarioCentralRepository.delete(inventario);
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────

    private InventarioCentralResponseDTO mapToResponseDTO(InventarioCentral inv) {
        return InventarioCentralResponseDTO.builder()
                .inventarioId(inv.getInventarioCentralId())
                .eppId(inv.getEpp().getEppId())
                .eppNombre(inv.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(null)
                .tipoUso(inv.getEpp().getTipoUso())
                .estadoId(inv.getEstado().getEstadoId())
                .estadoNombre(inv.getEstado().getNombre())
                .estadoDescripcion(inv.getEstado().getDescripcion())
                .estadoPermiteUso(inv.getEstado().getPermiteUso())
                .estadoColorHex(inv.getEstado().getColorHex())
                .cantidadActual(inv.getCantidadActual())
                .cantidadMinima(inv.getCantidadMinima())
                .cantidadMaxima(inv.getCantidadMaxima())
                .ubicacionBodega(inv.getUbicacionBodega())
                .lote(inv.getLote())
                .fechaAdquisicion(inv.getFechaAdquisicion())
                .costoUnitario(inv.getCostoUnitario())
                .proveedor(inv.getProveedor())
                .fechaVencimiento(inv.getFechaVencimiento())
                .observaciones(inv.getObservaciones())
                .ultimaActualizacion(inv.getUltimaActualizacion())
                .necesitaReposicion(inv.necesitaReposicion())
                .diasParaVencer(inv.getFechaVencimiento() != null
                        ? (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), inv.getFechaVencimiento())
                        : null)
                .build();
    }
}
