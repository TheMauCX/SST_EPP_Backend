package pe.edu.upeu.epp.service;

import lombok.RequiredArgsConstructor;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de entregas de EPP.
 *
 * Sprint 4b:
 *   - Se elimina el concepto de "jefe de área".
 *   - El supervisor que registra la entrega se obtiene del token JWT.
 *   - El área de descuento se deriva del área del trabajador receptor (Opción B).
 *   - El supervisor no está restringido a ningún área.
 */
@Service
@RequiredArgsConstructor
public class EntregaEppService {

    private final EntregaEppRepository entregaEppRepository;
    private final DetalleEntregaEppRepository detalleEntregaEppRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final CatalogoTallaRepository tallaRepository;
    private final InventarioAreaRepository inventarioAreaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InstanciaEppRepository instanciaEppRepository;
    private final EstadoEppRepository estadoEppRepository;

    @Transactional
    public EntregaEppResponseDTO registrarEntregaEpp(EntregaEppRequestDTO request, String username) {

        // Supervisor: usuario autenticado (no necesita estar ligado a un área)
        Usuario supervisor = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + username));

        // Trabajador receptor: su área determina de dónde se descuenta el stock
        Trabajador trabajador = trabajadorRepository.findById(request.getTrabajadorId())
                .orElseThrow(() -> new BusinessException(
                        "Trabajador no encontrado con ID: " + request.getTrabajadorId()));

        Area areaTrabajador = trabajador.getArea();
        if (areaTrabajador == null) {
            throw new BusinessException("El trabajador ID=" + request.getTrabajadorId()
                    + " no tiene área asignada.");
        }

        EntregaEpp entrega = new EntregaEpp();
        entrega.setSupervisorUsuario(supervisor);
        entrega.setTrabajador(trabajador);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setTipoEntrega(request.getTipoEntrega());
        entrega.setObservaciones(request.getObservaciones());

        EntregaEpp entregaGuardada = entregaEppRepository.save(entrega);
        List<DetalleEntregaDTO> detallesDTO = new ArrayList<>();

        for (ItemEntregaDTO item : request.getItems()) {
            CatalogoEpp epp = catalogoEppRepository.findById(item.getEppId())
                    .orElseThrow(() -> new BusinessException("EPP no encontrado: " + item.getEppId()));

            EstadoEpp estado = estadoEppRepository.findByNombre(item.getEstadoNombre())
                    .orElseThrow(() -> new BusinessException("Estado '" + item.getEstadoNombre() + "' no configurado."));

            CatalogoTalla talla = null;
            if (item.getTallaId() != null) {
                talla = tallaRepository.findById(item.getTallaId())
                        .orElseThrow(() -> new BusinessException("Talla no encontrada: " + item.getTallaId()));
            }

            // Buscar stock en el área del trabajador
            InventarioArea invArea = buscarInventarioArea(epp, areaTrabajador, estado, talla);

            if (invArea.getCantidadActual() < item.getCantidad()) {
                throw new BusinessException(String.format(
                        "Stock insuficiente para '%s'%s en área '%s'. Disponible: %d, Solicitado: %d.",
                        epp.getNombreEpp(),
                        talla != null ? " talla " + talla.getNombre() : "",
                        areaTrabajador.getNombreArea(),
                        invArea.getCantidadActual(), item.getCantidad()));
            }

            invArea.setCantidadActual(invArea.getCantidadActual() - item.getCantidad());
            inventarioAreaRepository.save(invArea);

            DetalleEntregaEpp detalle = new DetalleEntregaEpp();
            detalle.setEntrega(entregaGuardada);
            detalle.setEpp(epp);
            detalle.setTalla(talla);
            detalle.setCantidad(item.getCantidad());
            detalle.setMotivo(item.getMotivo());
            DetalleEntregaEpp detalleGuardado = detalleEntregaEppRepository.save(detalle);

            if (epp.getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                for (int i = 0; i < item.getCantidad(); i++) {
                    InstanciaEpp inst = new InstanciaEpp();
                    inst.setDetalleEntrega(detalleGuardado);
                    inst.setEpp(epp);
                    inst.setEstado(estado);
                    inst.setTrabajadorActual(trabajador);
                    inst.setAreaActual(areaTrabajador);
                    inst.setCodigoSerie(UUID.randomUUID().toString());
                    inst.setFechaAdquisicion(LocalDate.now());
                    detallesDTO.add(convertirInstancia(instanciaEppRepository.save(inst)));
                }
            } else {
                detallesDTO.add(convertirDetalle(detalleGuardado));
            }
        }

        return toResponseDTO(entregaGuardada, detallesDTO);
    }

    private InventarioArea buscarInventarioArea(CatalogoEpp epp, Area area,
                                                EstadoEpp estado, CatalogoTalla talla) {
        if (talla != null) {
            return inventarioAreaRepository
                    .findByEppAndAreaAndEstadoAndTalla(epp, area, estado, talla)
                    .orElseThrow(() -> new BusinessException(String.format(
                            "Sin stock '%s' para '%s' talla '%s' en área '%s'.",
                            estado.getNombre(), epp.getNombreEpp(),
                            talla.getNombre(), area.getNombreArea())));
        }
        return inventarioAreaRepository
                .findByEppAndAreaAndEstadoSinTalla(epp, area, estado)
                .orElseThrow(() -> new BusinessException(String.format(
                        "Sin stock '%s' para '%s' en área '%s'.",
                        estado.getNombre(), epp.getNombreEpp(), area.getNombreArea())));
    }

    // ── Consultas ──────────────────────────────────────────────────────────────

    public Page<EntregaEppResponseDTO> findAllEntregas(Pageable pageable) {
        return entregaEppRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public EntregaDetalleResponseDTO findEntregaById(Integer id) {
        EntregaEpp entrega = entregaEppRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Entrega no encontrada: " + id));

        List<DetalleEntregaDTO> detalles = new ArrayList<>();
        for (DetalleEntregaEpp d : detalleEntregaEppRepository.findByEntrega(entrega)) {
            if (d.getEpp().getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                instanciaEppRepository.findByDetalleEntrega(d).forEach(i -> detalles.add(convertirInstancia(i)));
            } else {
                detalles.add(convertirDetalle(d));
            }
        }

        int total = detalles.stream().mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum();

        return EntregaDetalleResponseDTO.builder()
                .entregaId(entrega.getEntregaId())
                .trabajadorId(entrega.getTrabajador().getTrabajadorId())
                .trabajadorNombre(entrega.getTrabajador().getNombres() + " " + entrega.getTrabajador().getApellidos())
                .trabajadorDni(entrega.getTrabajador().getDni())
                .trabajadorArea(entrega.getTrabajador().getArea() != null ? entrega.getTrabajador().getArea().getNombreArea() : null)
                .trabajadorPuesto(entrega.getTrabajador().getCargo())
                .supervisorId(entrega.getSupervisorUsuario() != null ? entrega.getSupervisorUsuario().getUsuarioId() : null)
                .supervisorNombre(resolverNombreSupervisor(entrega.getSupervisorUsuario()))
                .fechaEntrega(entrega.getFechaEntrega())
                .tipoEntrega(entrega.getTipoEntrega())
                .observaciones(entrega.getObservaciones())
                .status(entrega.getStatus())
                .items(detalles)
                .totalItems(total)
                .build();
    }

    public Page<EntregaEppResponseDTO> findEntregasByTrabajadorId(Integer trabajadorId, Pageable pageable) {
        Trabajador t = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new BusinessException("Trabajador no encontrado: " + trabajadorId));
        return entregaEppRepository.findByTrabajador(t, pageable).map(this::toResponseDTO);
    }

    public Page<EntregaEppResponseDTO> findEntregasByTrabajadorDni(String dni, Pageable pageable) {
        Trabajador t = trabajadorRepository.findByDni(dni)
                .orElseThrow(() -> new BusinessException("Trabajador no encontrado con DNI: " + dni));
        return entregaEppRepository.findByTrabajador(t, pageable).map(this::toResponseDTO);
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    private EntregaEppResponseDTO toResponseDTO(EntregaEpp entrega) {
        List<DetalleEntregaDTO> detalles = new ArrayList<>();
        for (DetalleEntregaEpp d : detalleEntregaEppRepository.findByEntrega(entrega)) {
            if (d.getEpp().getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                instanciaEppRepository.findByDetalleEntrega(d).forEach(i -> detalles.add(convertirInstancia(i)));
            } else {
                detalles.add(convertirDetalle(d));
            }
        }
        return toResponseDTO(entrega, detalles);
    }

    private EntregaEppResponseDTO toResponseDTO(EntregaEpp entrega, List<DetalleEntregaDTO> detalles) {
        return EntregaEppResponseDTO.builder()
                .entregaId(entrega.getEntregaId())
                .trabajadorId(entrega.getTrabajador().getTrabajadorId())
                .trabajadorNombre(entrega.getTrabajador().getNombres() + " " + entrega.getTrabajador().getApellidos())
                .trabajadorDni(entrega.getTrabajador().getDni())
                .supervisorId(entrega.getSupervisorUsuario() != null ? entrega.getSupervisorUsuario().getUsuarioId() : null)
                .supervisorNombre(resolverNombreSupervisor(entrega.getSupervisorUsuario()))
                .fechaEntrega(entrega.getFechaEntrega())
                .tipoEntrega(entrega.getTipoEntrega())
                .status(entrega.getStatus())
                .observaciones(entrega.getObservaciones())
                .detalles(detalles)
                .build();
    }

    private DetalleEntregaDTO convertirDetalle(DetalleEntregaEpp d) {
        return DetalleEntregaDTO.builder()
                .detalleId(d.getDetalleId())
                .eppId(d.getEpp().getEppId())
                .eppNombre(d.getEpp().getNombreEpp())
                .tipoUso(d.getEpp().getTipoUso())
                .cantidad(d.getCantidad())
                .motivo(d.getMotivo())
                .eppMarca(d.getEpp().getFabricante())
                .eppUnidadMedida(null)
                .build();
    }

    private DetalleEntregaDTO convertirInstancia(InstanciaEpp inst) {
        DetalleEntregaEpp d = inst.getDetalleEntrega();
        return DetalleEntregaDTO.builder()
                .detalleId(d != null ? d.getDetalleId() : null)
                .eppId(inst.getEpp().getEppId())
                .eppNombre(inst.getEpp().getNombreEpp())
                .tipoUso(inst.getEpp().getTipoUso())
                .cantidad(1)
                .instanciaEppId(inst.getInstanciaEppId())
                .eppCodigoSerie(inst.getCodigoSerie())
                .motivo(d != null ? d.getMotivo() : null)
                .eppMarca(inst.getEpp().getFabricante())
                .eppUnidadMedida(null)
                .build();
    }

    private String resolverNombreSupervisor(Usuario supervisor) {
        if (supervisor == null) return null;
        if (supervisor.getNombreCompleto() != null && !supervisor.getNombreCompleto().isBlank())
            return supervisor.getNombreCompleto();
        return supervisor.getNombreUsuario();
    }
}
