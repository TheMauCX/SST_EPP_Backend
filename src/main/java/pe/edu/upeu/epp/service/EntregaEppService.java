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
 * Fix (tallas):
 *   Al descontar del inventario de área se usa el tallaId del ItemEntregaDTO
 *   para buscar el registro exacto (EPP + área + estado + talla).
 *   Sin esto, si el área tiene 10 cascos-M y 8 cascos-L, entregar "1 casco-M"
 *   podría descontar del primer registro que encuentre sin importar la talla.
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
    public EntregaEppResponseDTO registrarEntregaEpp(EntregaEppRequestDTO entregaEppRequestDTO,
                                                      String username) {

        Usuario jefeAreaUsuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + username));

        Area areaDelJefe = jefeAreaUsuario.getTrabajador() != null
                ? jefeAreaUsuario.getTrabajador().getArea()
                : null;

        if (areaDelJefe == null) {
            throw new BusinessException("El usuario '" + username +
                    "' no está asignado a un área y no puede descontar stock.");
        }

        Trabajador trabajador = trabajadorRepository.findById(entregaEppRequestDTO.getTrabajadorId())
                .orElseThrow(() -> new BusinessException(
                        "Trabajador no encontrado con ID: " + entregaEppRequestDTO.getTrabajadorId()));

        EntregaEpp entrega = new EntregaEpp();
        entrega.setJefeArea(jefeAreaUsuario);
        entrega.setTrabajador(trabajador);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setTipoEntrega(entregaEppRequestDTO.getTipoEntrega());
        entrega.setObservaciones(entregaEppRequestDTO.getObservaciones());

        EntregaEpp entregaGuardada = entregaEppRepository.save(entrega);
        List<DetalleEntregaDTO> detallesRespuestaDTO = new ArrayList<>();

        for (ItemEntregaDTO item : entregaEppRequestDTO.getItems()) {

            CatalogoEpp catalogoEpp = catalogoEppRepository.findById(item.getEppId())
                    .orElseThrow(() -> new BusinessException(
                            "EPP no encontrado con ID: " + item.getEppId()));

            // Resolver estado
            EstadoEpp estadoDelItem = estadoEppRepository.findByNombre(item.getEstadoNombre())
                    .orElseThrow(() -> new BusinessException(
                            "Estado '" + item.getEstadoNombre() + "' no está configurado."));

            // Resolver talla (puede ser null)
            CatalogoTalla tallaDelItem = null;
            if (item.getTallaId() != null) {
                tallaDelItem = tallaRepository.findById(item.getTallaId())
                        .orElseThrow(() -> new BusinessException(
                                "Talla no encontrada con ID: " + item.getTallaId()));
            }

            // Buscar el registro de inventario del área con la talla correcta
            InventarioArea inventarioArea = buscarInventarioArea(
                    catalogoEpp, areaDelJefe, estadoDelItem, tallaDelItem);

            // Validar stock
            if (inventarioArea.getCantidadActual() < item.getCantidad()) {
                throw new BusinessException(String.format(
                        "Stock insuficiente para '%s'%s. Solicitado: %d, Disponible: %d.",
                        catalogoEpp.getNombreEpp(),
                        tallaDelItem != null ? " talla " + tallaDelItem.getNombre() : "",
                        item.getCantidad(),
                        inventarioArea.getCantidadActual()));
            }

            // Descontar stock del área
            inventarioArea.setCantidadActual(inventarioArea.getCantidadActual() - item.getCantidad());
            inventarioAreaRepository.save(inventarioArea);

            // Crear detalle de entrega (con talla)
            DetalleEntregaEpp detalle = new DetalleEntregaEpp();
            detalle.setEntrega(entregaGuardada);
            detalle.setEpp(catalogoEpp);
            detalle.setTalla(tallaDelItem);
            detalle.setCantidad(item.getCantidad());
            detalle.setMotivo(item.getMotivo());
            DetalleEntregaEpp detalleGuardado = detalleEntregaEppRepository.save(detalle);

            // Para DURADEROS crear instancias individuales
            if (catalogoEpp.getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                for (int i = 0; i < item.getCantidad(); i++) {
                    InstanciaEpp instancia = new InstanciaEpp();
                    instancia.setDetalleEntrega(detalleGuardado);
                    instancia.setEpp(catalogoEpp);
                    instancia.setEstado(estadoDelItem);
                    instancia.setTrabajadorActual(trabajador);
                    instancia.setAreaActual(trabajador.getArea());
                    instancia.setCodigoSerie(UUID.randomUUID().toString());
                    instancia.setFechaAdquisicion(LocalDate.now());
                    detallesRespuestaDTO.add(
                            convertirInstanciaADetalleDTO(instanciaEppRepository.save(instancia)));
                }
            } else {
                detallesRespuestaDTO.add(convertirDetalleConsumibleADetalleDTO(detalleGuardado));
            }
        }

        return convertirAEntregaResponseDTO(entregaGuardada, detallesRespuestaDTO);
    }

    /**
     * Busca el registro de inventario de área con la combinación correcta.
     * Con talla:  findByEppAndAreaAndEstadoAndTalla
     * Sin talla:  findByEppAndAreaAndEstadoSinTalla
     */
    private InventarioArea buscarInventarioArea(CatalogoEpp epp,
                                                Area area,
                                                EstadoEpp estado,
                                                CatalogoTalla talla) {
        if (talla != null) {
            return inventarioAreaRepository
                    .findByEppAndAreaAndEstadoAndTalla(epp, area, estado, talla)
                    .orElseThrow(() -> new BusinessException(String.format(
                            "Stock '%s' no encontrado para '%s' talla '%s' en área '%s'.",
                            estado.getNombre(), epp.getNombreEpp(),
                            talla.getNombre(), area.getNombreArea())));
        } else {
            return inventarioAreaRepository
                    .findByEppAndAreaAndEstadoSinTalla(epp, area, estado)
                    .orElseThrow(() -> new BusinessException(String.format(
                            "Stock '%s' no encontrado para '%s' en área '%s'.",
                            estado.getNombre(), epp.getNombreEpp(), area.getNombreArea())));
        }
    }

    // ── Consultas ──────────────────────────────────────────────────────────

    public Page<EntregaEppResponseDTO> findAllEntregas(Pageable pageable) {
        return entregaEppRepository.findAll(pageable).map(this::convertirAEntregaResponseDTO);
    }

    public EntregaDetalleResponseDTO findEntregaById(Integer id) {
        EntregaEpp entregaEpp = entregaEppRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Entrega no encontrada con ID: " + id));

        List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entregaEpp);
        List<DetalleEntregaDTO> detallesDTO = new ArrayList<>();

        for (DetalleEntregaEpp detalle : detalles) {
            if (detalle.getEpp().getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                instanciaEppRepository.findByDetalleEntrega(detalle)
                        .forEach(i -> detallesDTO.add(convertirInstanciaADetalleDTO(i)));
            } else {
                detallesDTO.add(convertirDetalleConsumibleADetalleDTO(detalle));
            }
        }

        Trabajador trabajador = entregaEpp.getTrabajador();
        Usuario jefeArea = entregaEpp.getJefeArea();
        String jefeAreaNombre = resolverNombreJefe(jefeArea);
        Integer jefeAreaId = jefeArea != null ? jefeArea.getUsuarioId() : null;
        int totalItems = detallesDTO.stream().mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum();

        return EntregaDetalleResponseDTO.builder()
                .entregaId(entregaEpp.getEntregaId())
                .trabajadorId(trabajador.getTrabajadorId())
                .trabajadorNombre(trabajador.getNombres() + " " + trabajador.getApellidos())
                .trabajadorDni(trabajador.getDni())
                .trabajadorArea(trabajador.getArea() != null ? trabajador.getArea().getNombreArea() : null)
                .trabajadorPuesto(trabajador.getCargo())
                .jefeAreaId(jefeAreaId)
                .jefeAreaNombre(jefeAreaNombre)
                .fechaEntrega(entregaEpp.getFechaEntrega())
                .tipoEntrega(entregaEpp.getTipoEntrega())
                .observaciones(entregaEpp.getObservaciones())
                .status(entregaEpp.getStatus())
                .items(detallesDTO)
                .totalItems(totalItems)
                .build();
    }

    public Page<EntregaEppResponseDTO> findEntregasByTrabajadorId(Integer trabajadorId, Pageable pageable) {
        Trabajador t = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new BusinessException("Trabajador no encontrado con ID: " + trabajadorId));
        return entregaEppRepository.findByTrabajador(t, pageable).map(this::convertirAEntregaResponseDTO);
    }

    public Page<EntregaEppResponseDTO> findEntregasByTrabajadorDni(String dni, Pageable pageable) {
        Trabajador t = trabajadorRepository.findByDni(dni)
                .orElseThrow(() -> new BusinessException("Trabajador no encontrado con DNI: " + dni));
        return entregaEppRepository.findByTrabajador(t, pageable).map(this::convertirAEntregaResponseDTO);
    }

    // ── Mapeo interno ──────────────────────────────────────────────────────

    private EntregaEppResponseDTO convertirAEntregaResponseDTO(EntregaEpp entrega) {
        List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entrega);
        List<DetalleEntregaDTO> detallesDTO = new ArrayList<>();
        for (DetalleEntregaEpp d : detalles) {
            if (d.getEpp().getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                instanciaEppRepository.findByDetalleEntrega(d)
                        .forEach(i -> detallesDTO.add(convertirInstanciaADetalleDTO(i)));
            } else {
                detallesDTO.add(convertirDetalleConsumibleADetalleDTO(d));
            }
        }
        return convertirAEntregaResponseDTO(entrega, detallesDTO);
    }

    private EntregaEppResponseDTO convertirAEntregaResponseDTO(EntregaEpp entrega,
                                                                List<DetalleEntregaDTO> detallesDTO) {
        return EntregaEppResponseDTO.builder()
                .entregaId(entrega.getEntregaId())
                .fechaEntrega(entrega.getFechaEntrega())
                .jefeAreaId(entrega.getJefeArea() != null
                        && entrega.getJefeArea().getTrabajador() != null
                        ? entrega.getJefeArea().getTrabajador().getTrabajadorId() : null)
                .jefeAreaNombre(resolverNombreJefe(entrega.getJefeArea()))
                .trabajadorId(entrega.getTrabajador().getTrabajadorId())
                .trabajadorDni(entrega.getTrabajador().getDni())
                .trabajadorNombre(entrega.getTrabajador().getNombres() + " "
                        + entrega.getTrabajador().getApellidos())
                .tipoEntrega(entrega.getTipoEntrega())
                .status(entrega.getStatus())
                .observaciones(entrega.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }

    private DetalleEntregaDTO convertirDetalleConsumibleADetalleDTO(DetalleEntregaEpp detalle) {
        return DetalleEntregaDTO.builder()
                .detalleId(detalle.getDetalleId())
                .eppId(detalle.getEpp().getEppId())
                .eppNombre(detalle.getEpp().getNombreEpp())
                .tipoUso(detalle.getEpp().getTipoUso())
                .cantidad(detalle.getCantidad())
                .motivo(detalle.getMotivo())
                .eppMarca(detalle.getEpp().getFabricante())
                .eppUnidadMedida(null)
                .build();
    }

    private DetalleEntregaDTO convertirInstanciaADetalleDTO(InstanciaEpp instancia) {
        DetalleEntregaEpp detalle = instancia.getDetalleEntrega();
        return DetalleEntregaDTO.builder()
                .detalleId(detalle != null ? detalle.getDetalleId() : null)
                .eppId(instancia.getEpp().getEppId())
                .eppNombre(instancia.getEpp().getNombreEpp())
                .tipoUso(instancia.getEpp().getTipoUso())
                .cantidad(1)
                .instanciaEppId(instancia.getInstanciaEppId())
                .eppCodigoSerie(instancia.getCodigoSerie())
                .motivo(detalle != null ? detalle.getMotivo() : null)
                .eppMarca(instancia.getEpp().getFabricante())
                .eppUnidadMedida(null)
                .build();
    }

    private String resolverNombreJefe(Usuario jefeArea) {
        if (jefeArea == null) return null;
        if (jefeArea.getTrabajador() != null) {
            return jefeArea.getTrabajador().getNombres() + " " + jefeArea.getTrabajador().getApellidos();
        }
        return jefeArea.getNombreUsuario();
    }
}
