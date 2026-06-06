package pe.edu.upeu.epp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.dto.request.CompraRequestDTO;
import pe.edu.upeu.epp.dto.request.DetalleCompraRequestDTO;
import pe.edu.upeu.epp.entity.*;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de registro de compras.
 *
 * Modelo de inventario corregido:
 *   Cada ítem de factura (EPP + talla) genera o actualiza UN registro
 *   en inventario_central cuya clave es (epp_id, lote, estado_id, talla_id).
 *
 *   Como el lote = nro_factura, dos facturas distintas NUNCA se fusionan
 *   aunque compren el mismo EPP y la misma talla. Esto preserva el precio
 *   por lote para los reportes financieros (HU-14).
 *
 *   Si dentro de la MISMA factura se envía el mismo (eppId + tallaId) dos
 *   veces (duplicado en el JSON), las cantidades se suman en el mismo
 *   registro porque comparten clave.
 *
 * IGV (HU-20):
 *   subtotal = montoTotal / 1.18  (HALF_UP, 2 decimales)
 *   igv      = montoTotal - subtotal
 */
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
                                  String username) {

        log.info("Iniciando registro de compra. Factura: {}", request.getNroFactura());

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + username));

        String rutaArchivo = null;
        if (archivoFactura != null && !archivoFactura.isEmpty()) {
            rutaArchivo = azureStorageService.subirFactura(archivoFactura, request.getNroFactura());
        }

        EstadoEpp estadoEnStock = estadoEppRepository.findByNombre("EN_STOCK")
                .orElseThrow(() -> new BusinessException("El estado EN_STOCK no está configurado."));

        // ── Calcular totales con IGV ──────────────────────────────────────

        BigDecimal montoTotal = BigDecimal.ZERO;
        for (DetalleCompraRequestDTO item : request.getItems()) {
            montoTotal = montoTotal.add(
                    item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
            );
        }

        BigDecimal subtotal = montoTotal.divide(DIVISOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv      = montoTotal.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

        log.info("Totales — Subtotal: {}, IGV: {}, Total: {}", subtotal, igv, montoTotal);

        // ── Construir entidad Compra ──────────────────────────────────────

        Compra compra = Compra.builder()
                .nroFactura(request.getNroFactura())
                .fechaCompra(request.getFechaCompra())
                .proveedor(request.getProveedor())
                .rutaArchivoFactura(rutaArchivo)
                .usuarioRegistro(usuario)
                .subtotal(subtotal)
                .igv(igv)
                .montoTotal(montoTotal)
                .fechaRegistro(LocalDateTime.now())
                .build();

        // ── Procesar cada ítem de la factura ─────────────────────────────

        for (DetalleCompraRequestDTO itemDTO : request.getItems()) {

            // 1. Resolver EPP
            CatalogoEpp epp = catalogoEppRepository.findById(itemDTO.getEppId())
                    .orElseThrow(() -> new BusinessException(
                            "EPP no encontrado en catálogo con ID: " + itemDTO.getEppId()));

            // 2. Resolver talla (puede ser null para EPPs sin talla)
            CatalogoTalla talla = null;
            if (itemDTO.getTallaId() != null) {
                talla = tallaRepository.findById(itemDTO.getTallaId())
                        .orElseThrow(() -> new BusinessException(
                                "Talla no encontrada con ID: " + itemDTO.getTallaId()));

                // Validar que la talla sea válida para este EPP
                // (solo si el EPP tiene tallas configuradas; si no tiene, igual aceptamos)
                if (!epp.getTallasDisponibles().isEmpty()
                        && epp.getTallasDisponibles().stream()
                               .noneMatch(t -> t.getTallaId().equals(itemDTO.getTallaId()))) {
                    throw new BusinessException(String.format(
                            "La talla '%s' no está configurada para el EPP '%s'. " +
                            "Tallas válidas: %s",
                            talla.getNombre(),
                            epp.getNombreEpp(),
                            epp.getTallasDisponibles().stream()
                               .map(CatalogoTalla::getNombre)
                               .sorted()
                               .reduce((a, b) -> a + ", " + b).orElse("-")
                    ));
                }
            }

            // 3. Calcular subtotal del ítem
            BigDecimal subtotalItem = itemDTO.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(itemDTO.getCantidad()));

            // 4. Construir detalle de compra (incluye talla)
            DetalleCompra detalle = DetalleCompra.builder()
                    .epp(epp)
                    .talla(talla)
                    .cantidad(itemDTO.getCantidad())
                    .precioUnitario(itemDTO.getPrecioUnitario())
                    .subtotal(subtotalItem)
                    .build();
            compra.addDetalle(detalle);

            // 5. Actualizar inventario central
            //    Clave: (epp, lote=nroFactura, estado=EN_STOCK, talla)
            //    Si ya existe ese lote+talla en esta factura (ítem duplicado), sumamos.
            //    Si es otra factura, la clave es distinta → se crea registro nuevo.
            actualizarInventarioCentral(
                    epp, talla, estadoEnStock, request, itemDTO
            );

            log.info("Ítem procesado: EPP='{}' talla='{}' cantidad={} precio={}",
                    epp.getNombreEpp(),
                    talla != null ? talla.getNombre() : "sin talla",
                    itemDTO.getCantidad(),
                    itemDTO.getPrecioUnitario());
        }

        return compraRepository.save(compra);
    }

    // ── Helpers privados ─────────────────────────────────────────────────

    /**
     * Busca el registro de inventario central con clave
     * (epp, lote, estado, talla) y lo actualiza, o crea uno nuevo.
     *
     * Con talla:    findByEppAndLoteAndEstadoAndTalla
     * Sin talla:    findByEppAndLoteAndEstadoSinTalla
     *
     * NUNCA mezcla lotes de facturas distintas aunque el EPP y la talla
     * sean iguales, porque el lote = nro_factura es diferente.
     */
    private void actualizarInventarioCentral(CatalogoEpp epp,
                                             CatalogoTalla talla,
                                             EstadoEpp estadoEnStock,
                                             CompraRequestDTO request,
                                             DetalleCompraRequestDTO itemDTO) {

        Optional<InventarioCentral> inventarioOpt;

        if (talla != null) {
            inventarioOpt = inventarioCentralRepository
                    .findByEppAndLoteAndEstadoAndTalla(epp, request.getNroFactura(), estadoEnStock, talla);
        } else {
            inventarioOpt = inventarioCentralRepository
                    .findByEppAndLoteAndEstadoSinTalla(epp, request.getNroFactura(), estadoEnStock);
        }

        if (inventarioOpt.isPresent()) {
            // Mismo EPP + misma talla + misma factura → sumar cantidad
            // (caso: ítem duplicado en el mismo JSON de la misma compra)
            InventarioCentral inv = inventarioOpt.get();
            inv.setCantidadActual(inv.getCantidadActual() + itemDTO.getCantidad());
            inv.setUltimaActualizacion(LocalDateTime.now());
            if (itemDTO.getCantidadMinima() != null) inv.setCantidadMinima(itemDTO.getCantidadMinima());
            if (itemDTO.getCantidadMaxima() != null) inv.setCantidadMaxima(itemDTO.getCantidadMaxima());
            if (itemDTO.getFechaVencimiento() != null) inv.setFechaVencimiento(itemDTO.getFechaVencimiento());
            inventarioCentralRepository.save(inv);
            log.info("Stock sumado en lote existente. EPP={} talla={} total={}",
                    epp.getNombreEpp(),
                    talla != null ? talla.getNombre() : "sin talla",
                    inv.getCantidadActual());
        } else {
            // Nueva factura o nueva talla → nuevo registro de inventario
            InventarioCentral nuevoInv = InventarioCentral.builder()
                    .epp(epp)
                    .talla(talla)
                    .estado(estadoEnStock)
                    .cantidadActual(itemDTO.getCantidad())
                    .cantidadMinima(itemDTO.getCantidadMinima() != null ? itemDTO.getCantidadMinima() : 10)
                    .cantidadMaxima(itemDTO.getCantidadMaxima() != null ? itemDTO.getCantidadMaxima() : 100)
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
            log.info("Nuevo registro en inventario central. EPP={} talla={} lote={} cant={}",
                    epp.getNombreEpp(),
                    talla != null ? talla.getNombre() : "sin talla",
                    request.getNroFactura(),
                    itemDTO.getCantidad());
        }
    }
}
