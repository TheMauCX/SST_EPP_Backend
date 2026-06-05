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
 * Sprint 4 — cambios (HU-20):
 *   - Se calcula y persiste subtotal e igv en la entidad Compra.
 *   - Fórmula peruana IGV 18%:
 *       subtotal = montoTotal / 1.18  (HALF_UP, 2 decimales)
 *       igv      = montoTotal - subtotal
 *
 * Sprint 4 — mejora Azure:
 *   - La factura se sube con subirFactura(file, nroFactura) para que
 *     quede en compras/facturas/ con nombre descriptivo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private static final BigDecimal DIVISOR_IGV = new BigDecimal("1.18");

    private final CompraRepository compraRepository;
    private final CatalogoEppRepository catalogoEppRepository;
    private final InventarioCentralRepository inventarioCentralRepository;
    private final EstadoEppRepository estadoEppRepository;
    private final UsuarioRepository usuarioRepository;
    private final AzureStorageService azureStorageService;

    @Transactional
    public Compra registrarCompra(CompraRequestDTO request, MultipartFile archivoFactura, String username) {
        log.info("Iniciando registro de compra. Factura: {}", request.getNroFactura());

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + username));

        // Subir factura con ruta organizada (HU-20 / mejora Azure)
        String rutaArchivo = null;
        if (archivoFactura != null && !archivoFactura.isEmpty()) {
            rutaArchivo = azureStorageService.subirFactura(archivoFactura, request.getNroFactura());
        }

        EstadoEpp estadoEnStock = estadoEppRepository.findByNombre("EN_STOCK")
                .orElseThrow(() -> new BusinessException("El estado EN_STOCK no está configurado."));

        // Calcular monto total bruto (suma de todos los ítems)
        BigDecimal montoTotal = BigDecimal.ZERO;
        for (DetalleCompraRequestDTO item : request.getItems()) {
            montoTotal = montoTotal.add(
                    item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
            );
        }

        // HU-20: Calcular IGV con precisión BigDecimal
        BigDecimal subtotal = montoTotal.divide(DIVISOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv      = montoTotal.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

        log.info("Totales calculados — Subtotal: {}, IGV: {}, Total: {}", subtotal, igv, montoTotal);

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

        for (DetalleCompraRequestDTO itemDTO : request.getItems()) {
            CatalogoEpp epp = catalogoEppRepository.findById(itemDTO.getEppId())
                    .orElseThrow(() -> new BusinessException("EPP no encontrado con ID: " + itemDTO.getEppId()));

            BigDecimal subtotalItem = itemDTO.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(itemDTO.getCantidad()));

            DetalleCompra detalle = DetalleCompra.builder()
                    .epp(epp)
                    .cantidad(itemDTO.getCantidad())
                    .precioUnitario(itemDTO.getPrecioUnitario())
                    .subtotal(subtotalItem)
                    .build();

            compra.addDetalle(detalle);

            // Actualizar inventario central (lote = nro factura, sin talla por defecto)
            Optional<InventarioCentral> inventarioOpt = inventarioCentralRepository
                    .findByEppAndLoteAndEstadoSinTalla(epp, request.getNroFactura(), estadoEnStock);

            if (inventarioOpt.isPresent()) {
                InventarioCentral inv = inventarioOpt.get();
                inv.setCantidadActual(inv.getCantidadActual() + itemDTO.getCantidad());
                inv.setUltimaActualizacion(LocalDateTime.now());
                if (itemDTO.getCantidadMinima() != null) inv.setCantidadMinima(itemDTO.getCantidadMinima());
                if (itemDTO.getCantidadMaxima() != null) inv.setCantidadMaxima(itemDTO.getCantidadMaxima());
                if (itemDTO.getFechaVencimiento() != null) inv.setFechaVencimiento(itemDTO.getFechaVencimiento());
                inventarioCentralRepository.save(inv);
            } else {
                InventarioCentral nuevoInv = InventarioCentral.builder()
                        .epp(epp)
                        .estado(estadoEnStock)
                        .talla(null)     // Sin talla; se crea por talla en un endpoint separado
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
            }
        }

        return compraRepository.save(compra);
    }
}
