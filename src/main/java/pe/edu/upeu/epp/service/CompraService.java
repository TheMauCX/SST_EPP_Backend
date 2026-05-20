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
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

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
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        String rutaArchivo = null;
        if (archivoFactura != null && !archivoFactura.isEmpty()) {
            rutaArchivo = azureStorageService.subirArchivo(archivoFactura);
        }

        Compra compra = Compra.builder()
                .nroFactura(request.getNroFactura())
                .fechaCompra(request.getFechaCompra())
                .proveedor(request.getProveedor())
                .rutaArchivoFactura(rutaArchivo)
                .usuarioRegistro(usuario)
                .fechaRegistro(LocalDateTime.now())
                .build();

        EstadoEpp estadoEnStock = estadoEppRepository.findByNombre("EN_STOCK")
                .orElseThrow(() -> new BusinessException("El estado EN_STOCK no está configurado en el sistema."));

        BigDecimal totalCompra = BigDecimal.ZERO;

        for (DetalleCompraRequestDTO itemDTO : request.getItems()) {

            CatalogoEpp epp = catalogoEppRepository.findById(itemDTO.getEppId())
                    .orElseThrow(() -> new BusinessException("EPP no encontrado en catálogo con ID: " + itemDTO.getEppId()));

            BigDecimal subtotal = itemDTO.getPrecioUnitario().multiply(BigDecimal.valueOf(itemDTO.getCantidad()));
            totalCompra = totalCompra.add(subtotal);

            DetalleCompra detalle = DetalleCompra.builder()
                    .epp(epp)
                    .cantidad(itemDTO.getCantidad())
                    .precioUnitario(itemDTO.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();

            compra.addDetalle(detalle);

            // Trazabilidad al Inventario Central
            Optional<InventarioCentral> inventarioOpt = inventarioCentralRepository.findByEppAndLoteAndEstado(epp, request.getNroFactura(), estadoEnStock);

            if (inventarioOpt.isPresent()) {
                InventarioCentral inv = inventarioOpt.get();
                inv.setCantidadActual(inv.getCantidadActual() + itemDTO.getCantidad());
                inv.setUltimaActualizacion(LocalDateTime.now());

                // Actualizar los parámetros logísticos si se envían en la nueva compra
                if (itemDTO.getCantidadMinima() != null) inv.setCantidadMinima(itemDTO.getCantidadMinima());
                if (itemDTO.getCantidadMaxima() != null) inv.setCantidadMaxima(itemDTO.getCantidadMaxima());
                if (itemDTO.getFechaVencimiento() != null) inv.setFechaVencimiento(itemDTO.getFechaVencimiento());
                if (itemDTO.getObservaciones() != null) inv.setObservaciones(itemDTO.getObservaciones());

                inventarioCentralRepository.save(inv);
                log.info("Stock actualizado en Inventario Central para EPP: {}", epp.getNombreEpp());
            } else {
                InventarioCentral nuevoInv = InventarioCentral.builder()
                        .epp(epp)
                        .estado(estadoEnStock)
                        .cantidadActual(itemDTO.getCantidad())
                        // Usamos los enviados en el DTO, o valores por defecto (10 y 100) si son nulos
                        .cantidadMinima(itemDTO.getCantidadMinima() != null ? itemDTO.getCantidadMinima() : 10)
                        .cantidadMaxima(itemDTO.getCantidadMaxima() != null ? itemDTO.getCantidadMaxima() : 100)
                        .fechaVencimiento(itemDTO.getFechaVencimiento())
                        .observaciones(itemDTO.getObservaciones())
                        .lote(request.getNroFactura())
                        .costoUnitario(BigDecimal.valueOf(itemDTO.getPrecioUnitario().doubleValue()))
                        .proveedor(request.getProveedor())
                        .ubicacionBodega("Almacén General") // Se mantiene por defecto
                        .fechaAdquisicion(request.getFechaCompra())
                        .fechaCreacion(LocalDateTime.now())
                        .ultimaActualizacion(LocalDateTime.now())
                        .build();
                inventarioCentralRepository.save(nuevoInv);
                log.info("Nuevo registro de Inventario Central creado para EPP: {}", epp.getNombreEpp());
            }
        }

        compra.setMontoTotal(totalCompra);
        return compraRepository.save(compra);
    }
}