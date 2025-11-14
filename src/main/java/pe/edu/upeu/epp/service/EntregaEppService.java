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

@Service
@RequiredArgsConstructor
public class EntregaEppService {

    // Repositorios principales
    private final EntregaEppRepository entregaEppRepository;
    private final DetalleEntregaEppRepository detalleEntregaEppRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final InventarioAreaRepository inventarioAreaRepository;
    private final UsuarioRepository usuarioRepository;

    // Repositorios para la nueva lógica (HU-8)
    private final InstanciaEppRepository instanciaEppRepository;
    private final EstadoEppRepository estadoEppRepository;

    @Transactional
    public EntregaEppResponseDTO registrarEntregaEpp(EntregaEppRequestDTO entregaEppRequestDTO, String username) {

        // --- PASO 1: OBTENER CONTEXTO (Usuario, Trabajador, Jefe de Área) ---
        Usuario jefeAreaUsuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + username));

        Area areaDelJefe;
        if (jefeAreaUsuario.getTrabajador() != null) {
            areaDelJefe = jefeAreaUsuario.getTrabajador().getArea();
        } else {
            areaDelJefe = null;
        }

        if (areaDelJefe == null) {
            throw new BusinessException("El Jefe de Área '" + username + "' no está asignado a un área y no puede descontar stock.");
        }

        Trabajador trabajador = trabajadorRepository.findById(entregaEppRequestDTO.getTrabajadorId())
                .orElseThrow(() -> new BusinessException("Trabajador no encontrado con ID: " + entregaEppRequestDTO.getTrabajadorId()));

        // --- PASO 2: OBTENER ESTADO INICIAL (Eliminado de aquí) ---
        // Ahora se obtiene por ítem, dentro del bucle.

        // --- PASO 3: CREAR EL REGISTRO MAESTRO DE ENTREGA ---
        EntregaEpp entrega = new EntregaEpp();
        entrega.setJefeArea(jefeAreaUsuario);
        entrega.setTrabajador(trabajador);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setTipoEntrega(entregaEppRequestDTO.getTipoEntrega());
        entrega.setObservaciones(entregaEppRequestDTO.getObservaciones());

        EntregaEpp entregaGuardada = entregaEppRepository.save(entrega);

        // Esta lista contendrá los DTOs de respuesta.
        List<DetalleEntregaDTO> detallesRespuestaDTO = new ArrayList<>();

        // --- PASO 4: PROCESAR CADA ITEM (Validar, Descontar, Crear Detalle, Crear Instancias) ---
        for (ItemEntregaDTO item : entregaEppRequestDTO.getItems()) {

            // a. Validar EPP
            CatalogoEpp catalogoEpp = catalogoEppRepository.findById(item.getEppId())
                    .orElseThrow(() -> new BusinessException("Catalogo EPP no encontrado con ID: " + item.getEppId()));

            // --- LÓGICA CORREGIDA (PASO 4.A-2) ---
            // Buscamos el estado solicitado (NUEVO, USADO, etc.) para ESTE ítem.
            String estadoSolicitadoNombre = item.getEstadoNombre();
            if (estadoSolicitadoNombre == null || estadoSolicitadoNombre.isBlank()) {
                throw new BusinessException("El ítem " + catalogoEpp.getNombreEpp() + " no especificó un 'estadoNombre' (ej. 'NUEVO' o 'USADO').");
            }

            EstadoEpp estadoDelItem = estadoEppRepository.findByNombre(estadoSolicitadoNombre)
                    .orElseThrow(() -> new BusinessException("Estado '" + estadoSolicitadoNombre + "' no está configurado en la base de datos (tabla estado_epp)."));

            // --- LÓGICA CORREGIDA (PASO 4.A-3) ---
            // Buscamos en el inventario el stock de ESE estado.
            InventarioArea inventarioArea = inventarioAreaRepository.findByAreaAndEppAndEstado(areaDelJefe, catalogoEpp, estadoDelItem)
                    .orElseThrow(() -> new BusinessException("Stock '" + estadoDelItem.getNombre() + "' no encontrado para '" + catalogoEpp.getNombreEpp() + "' en el área '" + areaDelJefe.getNombreArea() + "'.")); // Corregido: getNombre()

            // b. Validar y Descontar Stock
            if (inventarioArea.getCantidadActual() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para: '" + catalogoEpp.getNombreEpp() + "' (Estado: " + estadoDelItem.getNombre() + "). Solicitado: " + item.getCantidad() + ", Disponible: " + inventarioArea.getCantidadActual());
            }

            inventarioArea.setCantidadActual(inventarioArea.getCantidadActual() - item.getCantidad());
            inventarioAreaRepository.save(inventarioArea);

            // c. Crear el Detalle de la Entrega
            DetalleEntregaEpp detalle = new DetalleEntregaEpp();
            detalle.setEntrega(entregaGuardada);
            detalle.setEpp(catalogoEpp);
            detalle.setCantidad(item.getCantidad());
            detalle.setMotivo(item.getMotivo()); // Añadido desde el DTO del ítem

            DetalleEntregaEpp detalleGuardado = detalleEntregaEppRepository.save(detalle);

            // --- PASO 5: LÓGICA DURADERO vs CONSUMIBLE ---
            if (catalogoEpp.getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                // Si es DURADERO (ej. Casco), creamos N instancias
                for (int i = 0; i < item.getCantidad(); i++) {
                    InstanciaEpp instancia = new InstanciaEpp();

                    instancia.setDetalleEntrega(detalleGuardado); // Trazabilidad
                    instancia.setEpp(catalogoEpp);

                    // --- LÓGICA CORREGIDA ---
                    // Se asigna el estado que se seleccionó (NUEVO o USADO)
                    instancia.setEstado(estadoDelItem);

                    instancia.setTrabajadorActual(trabajador);
                    instancia.setAreaActual(trabajador.getArea());
                    instancia.setCodigoSerie(UUID.randomUUID().toString());
                    instancia.setFechaAdquisicion(LocalDate.now());

                    InstanciaEpp instanciaGuardada = instanciaEppRepository.save(instancia);
                    detallesRespuestaDTO.add(convertirInstanciaADetalleDTO(instanciaGuardada));
                }
            } else {
                // Si es CONSUMIBLE (ej. Guantes), solo añadimos 1 DTO de respuesta
                detallesRespuestaDTO.add(convertirDetalleConsumibleADetalleDTO(detalleGuardado));
            }
        }

        // --- PASO 6: CONSTRUIR Y DEVOLVER LA RESPUESTA ---
        return convertirAEntregaResponseDTO(entregaGuardada, detallesRespuestaDTO);
    }

    // --- Métodos de Consulta (ACTUALIZADOS CON PAGINACIÓN) ---

    public Page<EntregaEppResponseDTO> findAllEntregas(Pageable pageable) {
        Page<EntregaEpp> entregaPage = entregaEppRepository.findAll(pageable);
        return entregaPage.map(this::convertirAEntregaResponseDTO);
    }

    public EntregaDetalleResponseDTO findEntregaById(Integer id) {
        EntregaEpp entregaEpp = entregaEppRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Entrega no encontrada con ID: " + id));

        List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entregaEpp);

        List<DetalleEntregaDTO> detallesRespuestaDTO = new ArrayList<>();
        for (DetalleEntregaEpp detalle : detalles) {
            if (detalle.getEpp().getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                List<InstanciaEpp> instancias = instanciaEppRepository.findByDetalleEntrega(detalle);
                for (InstanciaEpp instancia : instancias) {
                    detallesRespuestaDTO.add(convertirInstanciaADetalleDTO(instancia));
                }
            } else {
                detallesRespuestaDTO.add(convertirDetalleConsumibleADetalleDTO(detalle));
            }
        }

        Trabajador trabajador = entregaEpp.getTrabajador();
        Usuario jefeArea = entregaEpp.getJefeArea();
        String jefeAreaNombre = null;
        Integer jefeAreaId = null;

        if (jefeArea != null) {
            jefeAreaId = jefeArea.getUsuarioId();
            if (jefeArea.getTrabajador() != null) {
                jefeAreaNombre = jefeArea.getTrabajador().getNombres() + " " + jefeArea.getTrabajador().getApellidos();
            } else {
                jefeAreaNombre = jefeArea.getNombreUsuario();
            }
        }

        int totalItems = detallesRespuestaDTO.stream().mapToInt(DetalleEntregaDTO::getCantidad).sum();

        return EntregaDetalleResponseDTO.builder()
                .entregaId(entregaEpp.getEntregaId())
                .trabajadorId(trabajador.getTrabajadorId())
                .trabajadorNombre(trabajador.getNombres() + " " + trabajador.getApellidos())
                .trabajadorDni(trabajador.getDni())
                // --- CORRECCIÓN LÍNEA 199 ---
                .trabajadorArea(trabajador.getArea() != null ? trabajador.getArea().getNombreArea() : null) // Corregido: getNombre()
                .trabajadorPuesto(trabajador.getCargo())
                .jefeAreaId(jefeAreaId)
                .jefeAreaNombre(jefeAreaNombre)
                .fechaEntrega(entregaEpp.getFechaEntrega())
                .tipoEntrega(entregaEpp.getTipoEntrega())
                .observaciones(entregaEpp.getObservaciones())
                .status(entregaEpp.getStatus())
                .items(detallesRespuestaDTO)
                .totalItems(totalItems)
                .build();
    }

    public Page<EntregaEppResponseDTO> findEntregasByTrabajadorId(Integer trabajadorId, Pageable pageable) {
        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new BusinessException("Trabajador no encontrado con ID: " + trabajadorId));

        Page<EntregaEpp> entregaPage = entregaEppRepository.findByTrabajador(trabajador, pageable);
        return entregaPage.map(this::convertirAEntregaResponseDTO);
    }

    public Page<EntregaEppResponseDTO> findEntregasByTrabajadorDni(String dni, Pageable pageable) {
        Trabajador trabajador = trabajadorRepository.findByDni(dni)
                .orElseThrow(() -> new BusinessException("Trabajador no encontrado con DNI: " + dni));

        Page<EntregaEpp> entregaPage = entregaEppRepository.findByTrabajador(trabajador, pageable);
        return entregaPage.map(this::convertirAEntregaResponseDTO);
    }


    // --- Métodos de Conversión (Helpers) ---

    private EntregaEppResponseDTO convertirAEntregaResponseDTO(EntregaEpp entrega) {
        List<DetalleEntregaEpp> detalles = detalleEntregaEppRepository.findByEntrega(entrega);

        List<DetalleEntregaDTO> detallesRespuestaDTO = new ArrayList<>();
        for (DetalleEntregaEpp detalle : detalles) {
            if (detalle.getEpp().getTipoUso() == CatalogoEpp.TipoUso.DURADERO) {
                List<InstanciaEpp> instancias = instanciaEppRepository.findByDetalleEntrega(detalle);
                for (InstanciaEpp instancia : instancias) {
                    detallesRespuestaDTO.add(convertirInstanciaADetalleDTO(instancia));
                }
            } else {
                detallesRespuestaDTO.add(convertirDetalleConsumibleADetalleDTO(detalle));
            }
        }

        return convertirAEntregaResponseDTO(entrega, detallesRespuestaDTO);
    }

    private EntregaEppResponseDTO convertirAEntregaResponseDTO(EntregaEpp entrega, List<DetalleEntregaDTO> detallesDTO) {

        String jefeAreaNombre = null;
        if (entrega.getJefeArea() != null) {
            if (entrega.getJefeArea().getTrabajador() != null) {
                jefeAreaNombre = entrega.getJefeArea().getTrabajador().getNombres() + " " + entrega.getJefeArea().getTrabajador().getApellidos();
            } else {
                jefeAreaNombre = entrega.getJefeArea().getNombreUsuario();
            }
        }

        return EntregaEppResponseDTO.builder()
                .entregaId(entrega.getEntregaId())
                .fechaEntrega(entrega.getFechaEntrega())
                .jefeAreaId(entrega.getJefeArea() != null ? entrega.getJefeArea().getTrabajador().getTrabajadorId() : null)
                .jefeAreaNombre(jefeAreaNombre)
                .trabajadorId(entrega.getTrabajador().getTrabajadorId())
                .trabajadorDni(entrega.getTrabajador().getDni())
                .trabajadorNombre(entrega.getTrabajador() != null ? entrega.getTrabajador().getNombres() + " " + entrega.getTrabajador().getApellidos() : null)
                .tipoEntrega(entrega.getTipoEntrega())
                .status(entrega.getStatus())
                .observaciones(entrega.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }

    private DetalleEntregaDTO convertirDetalleConsumibleADetalleDTO(DetalleEntregaEpp detalle) {
        CatalogoEpp epp = detalle.getEpp();
        return DetalleEntregaDTO.builder()
                .detalleId(detalle.getDetalleId())
                .eppId(epp.getEppId())
                .eppNombre(epp.getNombreEpp())
                .tipoUso(epp.getTipoUso())
                .cantidad(detalle.getCantidad())
                .instanciaEppId(null)
                .eppCodigoSerie(null)
                .motivo(detalle.getMotivo())
                .eppMarca(epp.getMarca())
                .eppUnidadMedida(epp.getUnidadMedida())
                .build();
    }

    private DetalleEntregaDTO convertirInstanciaADetalleDTO(InstanciaEpp instancia) {
        CatalogoEpp epp = instancia.getEpp();
        DetalleEntregaEpp detalle = instancia.getDetalleEntrega();

        return DetalleEntregaDTO.builder()
                .detalleId(detalle != null ? detalle.getDetalleId() : null)
                .eppId(epp.getEppId())
                .eppNombre(epp.getNombreEpp())
                .tipoUso(epp.getTipoUso())
                .cantidad(1)
                .instanciaEppId(instancia.getInstanciaEppId())
                .eppCodigoSerie(instancia.getCodigoSerie())
                .motivo(detalle != null ? detalle.getMotivo() : null)
                .eppMarca(epp.getMarca())
                .eppUnidadMedida(epp.getUnidadMedida())
                .build();
    }
}