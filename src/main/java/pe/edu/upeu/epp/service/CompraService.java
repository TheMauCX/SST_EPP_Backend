package pe.edu.upeu.epp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.dto.request.CompraRequestDTO;
import pe.edu.upeu.epp.dto.request.DetalleCompraRequestDTO;
import pe.edu.upeu.epp.dto.response.CompraDocumentosDTO;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private static final BigDecimal DIVISOR_IGV = new BigDecimal("1.18");

    private final CompraRepository compraRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final CatalogoTallaRepository tallaRepository;
    private final InventarioCentralRepository inventarioCentralRepository;
    private final EstadoEppRepository estadoEppRepository;
    private final UsuarioRepository usuarioRepository;
    private final AzureStorageService azureStorageService;

    @Transactional
    public Compra registrarCompra(CompraRequestDTO request,
                                  MultipartFile archivoFactura,
                                  MultipartFile archivoCotizacion,
                                  String username) {

        log.info("Registrando compra. Factura: {}", request.getNroFactura());

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + username));

        String rutaFactura = null;
        if (archivoFactura != null && !archivoFactura.isEmpty()) {
            rutaFactura = azureStorageService.subirFactura(archivoFactura, request.getNroFactura());
        }

        String rutaCotizacion = null;
        if (archivoCotizacion != null && !archivoCotizacion.isEmpty()) {
            rutaCotizacion = azureStorageService.subirCotizacion(archivoCotizacion, request.getNroFactura());
            log.info("Cotización subida: {}", rutaCotizacion);
        }

        EstadoEpp estadoEnStock = estadoEppRepository.findByNombre("EN_STOCK")
                .orElseThrow(() -> new BusinessException("Estado EN_STOCK no configurado."));

        BigDecimal montoTotal = BigDecimal.ZERO;
        for (DetalleCompraRequestDTO item : request.getItems()) {
            montoTotal = montoTotal.add(
                    item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
        }
        BigDecimal subtotal = montoTotal.divide(DIVISOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv      = montoTotal.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

        Compra compra = Compra.builder()
                .nroFactura(request.getNroFactura())
                .fechaCompra(request.getFechaCompra())
                .proveedor(request.getProveedor())
                .rutaArchivoFactura(rutaFactura)
                .rutaArchivoCotizacion(rutaCotizacion)
                .usuarioRegistro(usuario)
                .subtotal(subtotal)
                .igv(igv)
                .montoTotal(montoTotal)
                .fechaRegistro(LocalDateTime.now())
                .build();

        for (DetalleCompraRequestDTO itemDTO : request.getItems()) {
            CatalogoEpp epp = catalogoEppRepository.findById(itemDTO.getEppId())
                    .orElseThrow(() -> new BusinessException(
                            "EPP no encontrado con ID: " + itemDTO.getEppId()));

            CatalogoTalla talla = null;
            if (itemDTO.getTallaId() != null) {
                talla = tallaRepository.findById(itemDTO.getTallaId())
                        .orElseThrow(() -> new BusinessException(
                                "Talla no encontrada con ID: " + itemDTO.getTallaId()));

                if (!epp.getTallasDisponibles().isEmpty()
                        && epp.getTallasDisponibles().stream()
                        .noneMatch(t -> t.getTallaId().equals(itemDTO.getTallaId()))) {
                    throw new BusinessException(String.format(
                            "La talla '%s' no está configurada para el EPP '%s'.",
                            talla.getNombre(), epp.getNombreEpp()));
                }
            }

            BigDecimal subtotalItem = itemDTO.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(itemDTO.getCantidad()));

            DetalleCompra detalle = DetalleCompra.builder()
                    .epp(epp).talla(talla)
                    .cantidad(itemDTO.getCantidad())
                    .precioUnitario(itemDTO.getPrecioUnitario())
                    .subtotal(subtotalItem)
                    .build();
            compra.addDetalle(detalle);

            actualizarInventarioCentral(epp, talla, estadoEnStock, request, itemDTO);
        }

        return compraRepository.save(compra);
    }

    // ── NUEVO MÉTODO ─────────────────────────────────────────────────────────

    /**
     * Busca los documentos (factura + cotización) de una compra por su número de factura.
     * El número de factura coincide con el campo "lote" del inventario central.
     *
     * @param nroFactura Número de factura / lote del inventario
     * @return Optional con los datos y URLs de la compra, vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<CompraDocumentosDTO> buscarDocumentosPorNroFactura(String nroFactura) {
        return compraRepository.findByNroFactura(nroFactura)
                .map(compra -> CompraDocumentosDTO.builder()
                        .compraId(compra.getCompraId())
                        .nroFactura(compra.getNroFactura())
                        .proveedor(compra.getProveedor())
                        .fechaCompra(compra.getFechaCompra())
                        .urlFactura(compra.getRutaArchivoFactura())
                        .urlCotizacion(compra.getRutaArchivoCotizacion())
                        .subtotal(compra.getSubtotal())
                        .igv(compra.getIgv())
                        .montoTotal(compra.getMontoTotal())
                        .build());
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void actualizarInventarioCentral(CatalogoEpp epp,
                                             CatalogoTalla talla,
                                             EstadoEpp estadoEnStock,
                                             CompraRequestDTO request,
                                             DetalleCompraRequestDTO itemDTO) {

        java.util.Optional<InventarioCentral> inventarioOpt = talla != null
                ? inventarioCentralRepository.findByEppAndLoteAndEstadoAndTalla(
                epp, request.getNroFactura(), estadoEnStock, talla)
                : inventarioCentralRepository.findByEppAndLoteAndEstadoSinTalla(
                epp, request.getNroFactura(), estadoEnStock);

        if (inventarioOpt.isPresent()) {
            InventarioCentral inv = inventarioOpt.get();
            inv.setCantidadActual(inv.getCantidadActual() + itemDTO.getCantidad());
            inv.setUltimaActualizacion(LocalDateTime.now());
            if (itemDTO.getFechaVencimiento() != null) inv.setFechaVencimiento(itemDTO.getFechaVencimiento());
            inventarioCentralRepository.save(inv);
        } else {
            InventarioCentral nuevoInv = InventarioCentral.builder()
                    .epp(epp).talla(talla).estado(estadoEnStock)
                    .cantidadActual(itemDTO.getCantidad())
                    .fechaVencimiento(itemDTO.getFechaVencimiento())
                    .observaciones(itemDTO.getObservaciones())
                    .lote(request.getNroFactura())
                    .costoUnitario(itemDTO.getPrecioUnitario())
                    .proveedor(request.getProveedor())
                    .ubicacionBodega("Almacén General")
                    .fechaAdquisicion(request.getFechaCompra())
                    .fechaCreacion(LocalDateTime.now())
                    .ultimaActualizacion(LocalDateTime.now())
                    .build();
            inventarioCentralRepository.save(nuevoInv);
        }
    }
}