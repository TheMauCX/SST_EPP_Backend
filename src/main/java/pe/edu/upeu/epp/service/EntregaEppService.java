package pe.edu.upeu.epp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.EntregaEppRequestDTO;
import pe.edu.upeu.epp.dto.request.ItemEntregaDTO;
import pe.edu.upeu.epp.dto.response.DetalleEntregaDTO;
import pe.edu.upeu.epp.dto.response.EntregaDetalleResponseDTO;
import pe.edu.upeu.epp.dto.response.EntregaEppResponseDTO;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para Entregas de EPP.
 * CRÍTICO: Garantiza integridad del inventario con transacciones atómicas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntregaEppService {

    private final EntregaEppRepository entregaEppRepository;
    private final DetalleEntregaEppRepository detalleEntregaEppRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final InventarioAreaRepository inventarioAreaRepository;
    private final InstanciaEppRepository instanciaEppRepository;
    private final EstadoEppRepository estadoEppRepository;

    /**
     * Registrar entrega de EPP (TRANSACCIÓN ATÓMICA).
     * Este es el flujo más crítico del sistema.
     */
    @Transactional(rollbackFor = Exception.class)
    public EntregaEppResponseDTO registrarEntrega(EntregaEppRequestDTO request) {
        log.info("==== INICIANDO REGISTRO DE ENTREGA ====");
        log.info("Trabajador ID: {}, Jefe Área ID: {}, Items: {}",
                request.getTrabajadorId(), request.getJefeAreaId(), request.getItems().size());

        // ========================================
        // 1. VALIDACIONES INICIALES
        // ========================================

        // Validar trabajador
        Trabajador trabajador = trabajadorRepository.findById(request.getTrabajadorId())
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado con ID: " + request.getTrabajadorId()));

        if (trabajador.getEstado() != Trabajador.EstadoTrabajador.ACTIVO) {
            throw new BusinessException("El trabajador no está activo. Estado: " + trabajador.getEstado());
        }

        // Validar jefe de área
        Usuario jefeAreaUsuario = usuarioRepository.findById(request.getJefeAreaId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario jefe de área no encontrado"));

        Trabajador jefeArea = jefeAreaUsuario.getTrabajador();
        if (jefeArea == null) {
            throw new BusinessException("El usuario no tiene un trabajador asociado");
        }

        // Validar que el jefe pertenece al área del trabajador
        if (!jefeArea.getArea().getAreaId().equals(trabajador.getArea().getAreaId())) {
            throw new BusinessException(
                    String.format("El jefe de área pertenece a '%s' pero el trabajador pertenece a '%s'",
                            jefeArea.getArea().getNombreArea(), trabajador.getArea().getNombreArea())
            );
        }

        log.debug("Validaciones iniciales completadas");

        // ========================================
        // 2. PROCESAR CADA ITEM (CRÍTICO)
        // ========================================

        List<DetalleEntregaEpp> detalles = new ArrayList<>();

        for (ItemEntregaDTO item : request.getItems()) {
            log.debug("Procesando item - EPP ID: {}", item.getEppId());

            CatalogoEpp epp = catalogoEppRepository.findById(item.getEppId())
                    .orElseThrow(() -> new EntityNotFoundException("EPP no encontrado con ID: " + item.getEppId()));

            DetalleEntregaEpp detalle;

            if (epp.getTipoUso() == CatalogoEpp.TipoUso.CONSUMIBLE) {
                // Procesar EPP CONSUMIBLE
                detalle = procesarEntregaConsumible(item, epp, trabajador.getArea());
            } else {
                // Procesar EPP DURADERO
                detalle = procesarEntregaDuradero(item, epp, trabajador);
            }

            detalles.add(detalle);
        }

        log.debug("Todos los items procesados exitosamente");

        // ========================================
        // 3. CREAR REGISTRO DE ENTREGA
        // ========================================

        EntregaEpp entrega = EntregaEpp.builder()
                .trabajador(trabajador)
                .jefeArea(jefeArea)
                .fechaEntrega(LocalDateTime.now())
                .tipoEntrega(request.getTipoEntrega())
                .observaciones(request.getObservaciones())
                .firmaDigital(request.getFirmaDigital())
                .status("COMPLETADA")
                .build();

        entrega = entregaEppRepository.save(entrega);
        log.info("Entrega creada con ID: {}", entrega.getEntregaId());

        // ========================================
        // 4. ASOCIAR DETALLES A LA ENTREGA
        // ========================================

        for (DetalleEntregaEpp detalle : detalles) {
            detalle.setEntrega(entrega);
        }
        detalleEntregaEppRepository.saveAll(detalles);

        log.info("==== ENTREGA COMPLETADA EXITOSAMENTE - ID: {} ====", entrega.getEntregaId());

        return mapToResponseDTO(entrega, detalles);
    }

    /**
     * Procesar entrega de EPP CONSUMIBLE.
     * Resta del inventario del área.
     */
    private DetalleEntregaEpp procesarEntregaConsumible(ItemEntregaDTO item, CatalogoEpp epp, Area area) {
        log.debug("Procesando EPP CONSUMIBLE: {} - Cantidad: {}", epp.getNombreEpp(), item.getCantidad());

        if (item.getCantidad() == null || item.getCantidad() <= 0) {
            throw new BusinessException("Para EPP consumible, la cantidad debe ser mayor a 0");
        }

        // Buscar inventario del área
        InventarioArea inventario = inventarioAreaRepository
                .findByEppIdAndAreaId(epp.getEppId(), area.getAreaId())
                .orElseThrow(() -> new BusinessException(
                        "No existe inventario de '" + epp.getNombreEpp() + "' en el área '" + area.getNombreArea() + "'"));

        // Verificar stock suficiente
        if (inventario.getCantidadActual() < item.getCantidad()) {
            throw new BusinessException(
                    String.format("Stock insuficiente. Disponible: %d, Solicitado: %d para '%s'",
                            inventario.getCantidadActual(), item.getCantidad(), epp.getNombreEpp())
            );
        }

        // RESTAR STOCK (CRÍTICO)
        inventario.setCantidadActual(inventario.getCantidadActual() - item.getCantidad());
        inventarioAreaRepository.save(inventario);

        log.info("Stock actualizado - EPP: {}, Nueva cantidad: {}",
                epp.getNombreEpp(), inventario.getCantidadActual());

        // Crear detalle
        return DetalleEntregaEpp.builder()
                .epp(epp)
                .cantidad(item.getCantidad())
                .motivo(item.getMotivo())
                .build();
    }

    /**
     * Procesar entrega de EPP DURADERO.
     * Actualiza el estado de la instancia.
     */
    private DetalleEntregaEpp procesarEntregaDuradero(ItemEntregaDTO item, CatalogoEpp epp, Trabajador trabajador) {
        log.debug("Procesando EPP DURADERO: {} - Instancia ID: {}", epp.getNombreEpp(), item.getInstanciaEppId());

        if (item.getInstanciaEppId() == null) {
            throw new BusinessException("Para EPP duradero, el ID de instancia es obligatorio");
        }

        // Buscar instancia
        InstanciaEpp instancia = instanciaEppRepository.findById(item.getInstanciaEppId())
                .orElseThrow(() -> new EntityNotFoundException("Instancia EPP no encontrada con ID: " + item.getInstanciaEppId()));

        // Verificar que sea del mismo tipo de EPP
        if (!instancia.getEpp().getEppId().equals(epp.getEppId())) {
            throw new BusinessException("La instancia no corresponde al tipo de EPP seleccionado");
        }

        // Verificar que esté disponible
        EstadoEpp estadoStock = estadoEppRepository.findByNombre("EN_STOCK")
                .orElseThrow(() -> new EntityNotFoundException("Estado EN_STOCK no encontrado"));

        if (!instancia.getEstado().getEstadoId().equals(estadoStock.getEstadoId())) {
            throw new BusinessException(
                    String.format("La instancia no está disponible. Estado actual: %s",
                            instancia.getEstado().getNombre())
            );
        }

        // ACTUALIZAR ESTADO A "ENTREGADO" (CRÍTICO)
        EstadoEpp estadoEntregado = estadoEppRepository.findByNombre("ENTREGADO")
                .orElseThrow(() -> new EntityNotFoundException("Estado ENTREGADO no encontrado"));

        instancia.setEstado(estadoEntregado);
        instancia.setTrabajadorActual(trabajador);
        instancia.setAreaActual(trabajador.getArea());
        instanciaEppRepository.save(instancia);

        log.info("Instancia actualizada - Código: {}, Estado: ENTREGADO, Trabajador: {}",
                instancia.getCodigoSerie(), trabajador.getNombres());

        // Crear detalle
        return DetalleEntregaEpp.builder()
                .epp(epp)
                .instanciaEpp(instancia)
                .cantidad(1)
                .motivo(item.getMotivo())
                .build();
    }

    /**
     * Obtener entrega por ID con detalles completos.
     */
    @Transactional(readOnly = true)
    public EntregaDetalleResponseDTO obtenerDetalle(Integer id) {
        log.debug("Obteniendo detalle de entrega: {}", id);

        EntregaEpp entrega = entregaEppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entrega no encontrada con ID: " + id));

        List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entrega);

        return mapToDetalleResponseDTO(entrega, detalles);
    }

    /**
     * Listar entregas con paginación.
     */
    @Transactional(readOnly = true)
    public Page<EntregaEppResponseDTO> listarTodas(Pageable pageable) {
        return entregaEppRepository.findAll(pageable)
                .map(entrega -> {
                    List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entrega);
                    return mapToResponseDTO(entrega, detalles);
                });
    }

    /**
     * Listar entregas por trabajador.
     */
    @Transactional(readOnly = true)
    public Page<EntregaEppResponseDTO> listarPorTrabajador(Integer trabajadorId, Pageable pageable) {
        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado"));

        return entregaEppRepository.findByTrabajador(trabajadorId, pageable)
                .map(entrega -> {
                    List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entrega);
                    return mapToResponseDTO(entrega, detalles);
                });
    }

    /**
     * Mapea a DTO de respuesta básica.
     */
    private EntregaEppResponseDTO mapToResponseDTO(EntregaEpp entrega, List<DetalleEntregaEpp> detalles) {
        return EntregaEppResponseDTO.builder()
                .entregaId(entrega.getEntregaId())
                .trabajadorId(entrega.getTrabajador().getTrabajadorId())
                .trabajadorNombre(entrega.getTrabajador().getNombres() + " " + entrega.getTrabajador().getApellidos())
                .trabajadorDni(entrega.getTrabajador().getDni())
                .jefeAreaId(entrega.getJefeArea().getTrabajadorId())
                .jefeAreaNombre(entrega.getJefeArea().getNombres() + " " + entrega.getJefeArea().getApellidos())
                .fechaEntrega(entrega.getFechaEntrega())
                .tipoEntrega(entrega.getTipoEntrega())
                .observaciones(entrega.getObservaciones())
                .status(entrega.getStatus())
                .items(detalles.stream().map(this::mapDetalleToDTO).collect(Collectors.toList()))
                .build();
    }

    /**
     * Mapea a DTO de respuesta detallada.
     */
    private EntregaDetalleResponseDTO mapToDetalleResponseDTO(EntregaEpp entrega, List<DetalleEntregaEpp> detalles) {
        return EntregaDetalleResponseDTO.builder()
                .entregaId(entrega.getEntregaId())
                .trabajadorId(entrega.getTrabajador().getTrabajadorId())
                .trabajadorNombre(entrega.getTrabajador().getNombres() + " " + entrega.getTrabajador().getApellidos())
                .trabajadorDni(entrega.getTrabajador().getDni())
                .trabajadorArea(entrega.getTrabajador().getArea().getNombreArea())
                .trabajadorPuesto(entrega.getTrabajador().getPuesto())
                .jefeAreaId(entrega.getJefeArea().getTrabajadorId())
                .jefeAreaNombre(entrega.getJefeArea().getNombres() + " " + entrega.getJefeArea().getApellidos())
                .fechaEntrega(entrega.getFechaEntrega())
                .tipoEntrega(entrega.getTipoEntrega())
                .observaciones(entrega.getObservaciones())
                .status(entrega.getStatus())
                .items(detalles.stream().map(this::mapDetalleToDTO).collect(Collectors.toList()))
                .totalItems(detalles.size())
                .build();
    }

    /**
     * Mapea detalle a DTO.
     */
    private DetalleEntregaDTO mapDetalleToDTO(DetalleEntregaEpp detalle) {
        return DetalleEntregaDTO.builder()
                .detalleId(detalle.getDetalleId())
                .eppId(detalle.getEpp().getEppId())
                .eppNombre(detalle.getEpp().getNombreEpp())
                .eppMarca(detalle.getEpp().getMarca())              // ← NUEVO
                .eppUnidadMedida(detalle.getEpp().getUnidadMedida())// ← NUEVO
                .tipoUso(detalle.getEpp().getTipoUso())
                .cantidad(detalle.getCantidad())
                .instanciaEppId(detalle.getInstanciaEpp() != null ? detalle.getInstanciaEpp().getInstanciaEppId() : null)
                .codigoSerie(detalle.getInstanciaEpp() != null ? detalle.getInstanciaEpp().getCodigoSerie() : null)
                .motivo(detalle.getMotivo())
                .build();
    }
}