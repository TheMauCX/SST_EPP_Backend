package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.AjusteInventarioDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralRequestDTO;
import pe.edu.upeu.epp.dto.request.InventarioCentralUpdateDTO;
import pe.edu.upeu.epp.dto.response.InventarioCentralResponseDTO;
import pe.edu.upeu.epp.entity.CatalogoEpp;
import pe.edu.upeu.epp.entity.EstadoEpp;
import pe.edu.upeu.epp.entity.InventarioCentral;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.CatalogoEppRepository;
import pe.edu.upeu.epp.repository.EstadoEppRepository;
import pe.edu.upeu.epp.repository.InventarioCentralRepository;

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

    @Transactional
    public InventarioCentralResponseDTO crear(InventarioCentralRequestDTO request) {
        log.info("Creando inventario central - EPP ID: {}, Lote: {}, Estado ID: {}",
                request.getEppId(), request.getLote(), request.getEstadoId());

        CatalogoEpp epp = catalogoEppRepository.findById(request.getEppId())
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + request.getEppId()));

        EstadoEpp estado = estadoEppRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        boolean existe = inventarioCentralRepository.existsByEppAndLoteAndEstado(epp, request.getLote(), estado);
        if (existe) {
            throw new BusinessException(
                    String.format("Ya existe un registro de inventario para EPP '%s', Lote '%s', Estado '%s'",
                            epp.getNombreEpp(), request.getLote(), estado.getNombre())
            );
        }

        InventarioCentral inventario = InventarioCentral.builder()
                .epp(epp)
                .estado(estado)
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

        InventarioCentral guardado = inventarioCentralRepository.save(inventario);
        log.info("Inventario central creado con ID: {}", guardado.getInventarioCentralId());

        return mapToResponseDTO(guardado);
    }

    @Transactional
    public InventarioCentralResponseDTO actualizar(Integer id, InventarioCentralUpdateDTO request) {
        log.info("Actualizando inventario central ID: {}", id);

        InventarioCentral inventarioActual = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        CatalogoEpp nuevoEpp = inventarioActual.getEpp();
        String nuevoLote = request.getLote() != null ? request.getLote() : inventarioActual.getLote();

        EstadoEpp nuevoEstado = inventarioActual.getEstado();
        if (request.getEstadoId() != null) {
            nuevoEstado = estadoEppRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));
        }

        boolean cambioLote = !nuevoLote.equals(inventarioActual.getLote());
        boolean cambioEstado = !nuevoEstado.getEstadoId().equals(inventarioActual.getEstado().getEstadoId());
        boolean hayCambiosClave = cambioLote || cambioEstado;

        log.debug("Cambios detectados - Lote: {}, Estado: {}", cambioLote, cambioEstado);

        if (hayCambiosClave) {
            log.info("Detectados cambios en campos clave. Buscando registro existente...");

            Optional<InventarioCentral> registroExistente =
                    inventarioCentralRepository.findByEppAndLoteAndEstado(nuevoEpp, nuevoLote, nuevoEstado);

            if (registroExistente.isPresent() && !registroExistente.get().getInventarioCentralId().equals(id)) {
                log.warn("Ya existe un registro con la nueva combinación. Ejecutando MERGE...");

                InventarioCentral destino = registroExistente.get();
                Integer cantidadAMover = inventarioActual.getCantidadActual();

                log.info("MERGE: Moviendo {} unidades del registro ID {} al registro ID {}",
                        cantidadAMover, id, destino.getInventarioCentralId());

                destino.setCantidadActual(destino.getCantidadActual() + cantidadAMover);

                if (request.getCantidadMinima() != null) {
                    destino.setCantidadMinima(request.getCantidadMinima());
                }
                if (request.getCantidadMaxima() != null) {
                    destino.setCantidadMaxima(request.getCantidadMaxima());
                }
                if (request.getUbicacionBodega() != null) {
                    destino.setUbicacionBodega(request.getUbicacionBodega());
                }
                if (request.getObservaciones() != null) {
                    String observacionMerge = String.format(
                            "[MERGE] %s unidades consolidadas desde registro ID %d. %s",
                            cantidadAMover, id, request.getObservaciones()
                    );
                    destino.setObservaciones(observacionMerge);
                }

                InventarioCentral consolidado = inventarioCentralRepository.save(destino);
                inventarioCentralRepository.delete(inventarioActual);

                return mapToResponseDTO(consolidado);
            }
        }

        if (request.getEstadoId() != null) {
            inventarioActual.setEstado(nuevoEstado);
        }
        if (request.getLote() != null) {
            inventarioActual.setLote(nuevoLote);
        }
        if (request.getCantidadMinima() != null) {
            inventarioActual.setCantidadMinima(request.getCantidadMinima());
        }
        if (request.getCantidadMaxima() != null) {
            inventarioActual.setCantidadMaxima(request.getCantidadMaxima());
        }
        if (request.getUbicacionBodega() != null) {
            inventarioActual.setUbicacionBodega(request.getUbicacionBodega());
        }
        if (request.getObservaciones() != null) {
            inventarioActual.setObservaciones(request.getObservaciones());
        }

        InventarioCentral actualizado = inventarioCentralRepository.save(inventarioActual);
        return mapToResponseDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public InventarioCentralResponseDTO obtenerPorId(Integer id) {
        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));
        return mapToResponseDTO(inventario);
    }

    @Transactional(readOnly = true)
    public Page<InventarioCentralResponseDTO> listarTodos(Pageable pageable) {
        return inventarioCentralRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarPorEpp(Integer eppId) {
        CatalogoEpp epp = catalogoEppRepository.findById(eppId)
                .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + eppId));

        return inventarioCentralRepository.findByEpp(epp).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarStockBajo() {
        return inventarioCentralRepository.findStockBajo().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventarioCentralResponseDTO> listarProximosAVencer() {
        LocalDate fechaLimite = LocalDate.now().plusDays(30);
        return inventarioCentralRepository.findByFechaVencimientoBefore(fechaLimite).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventarioCentralResponseDTO ajustarStock(Integer id, AjusteInventarioDTO request) {
        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        Integer cantidadAnterior = inventario.getCantidadActual();
        Integer ajuste = request.getCantidadAjuste();

        if ("SALIDA".equals(request.getTipoAjuste())) {
            ajuste = -ajuste;
        }

        Integer nuevaCantidad = cantidadAnterior + ajuste;

        if (nuevaCantidad < 0) {
            throw new BusinessException("El ajuste resultaría en cantidad negativa");
        }

        inventario.setCantidadActual(nuevaCantidad);
        inventario.setObservaciones(String.format("[AJUSTE %s] %d unidades. Motivo: %s", request.getTipoAjuste(), Math.abs(ajuste), request.getMotivo()));

        return mapToResponseDTO(inventarioCentralRepository.save(inventario));
    }

    @Transactional
    public void eliminar(Integer id) {
        InventarioCentral inventario = inventarioCentralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        if (inventario.getCantidadActual() > 0) {
            throw new BusinessException("No se puede eliminar inventario con stock disponible");
        }
        inventarioCentralRepository.delete(inventario);
    }

    private InventarioCentralResponseDTO mapToResponseDTO(InventarioCentral inventario) {
        return InventarioCentralResponseDTO.builder()
                .inventarioId(inventario.getInventarioCentralId())
                .eppId(inventario.getEpp().getEppId())
                .eppNombre(inventario.getEpp().getNombreEpp())
                .eppCodigoIdentificacion(null) // Campo eliminado de CatalogoEpp
                .tipoUso(inventario.getEpp().getTipoUso())
                .estadoId(inventario.getEstado().getEstadoId())
                .estadoNombre(inventario.getEstado().getNombre())
                .estadoDescripcion(inventario.getEstado().getDescripcion())
                .estadoPermiteUso(inventario.getEstado().getPermiteUso())
                .estadoColorHex(inventario.getEstado().getColorHex())
                .cantidadActual(inventario.getCantidadActual())
                .cantidadMinima(inventario.getCantidadMinima())
                .cantidadMaxima(inventario.getCantidadMaxima())
                .ubicacionBodega(inventario.getUbicacionBodega())
                .lote(inventario.getLote())
                .fechaAdquisicion(inventario.getFechaAdquisicion())
                .costoUnitario(inventario.getCostoUnitario())
                .proveedor(inventario.getProveedor())
                .fechaVencimiento(inventario.getFechaVencimiento())
                .observaciones(inventario.getObservaciones())
                .ultimaActualizacion(inventario.getUltimaActualizacion())
                .necesitaReposicion(inventario.necesitaReposicion())
                .diasParaVencer(inventario.getFechaVencimiento() != null ?
                        (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), inventario.getFechaVencimiento()) : null)
                .build();
    }
}