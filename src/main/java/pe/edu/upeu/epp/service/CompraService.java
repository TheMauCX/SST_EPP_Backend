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
    private final FileStorageService fileStorageService;

    // ATENCIÓN: @Transactional garantiza que si falla el archivo o el stock, la compra entera hace Rollback
    @Transactional
    public Compra registrarCompra(CompraRequestDTO request, MultipartFile archivoFactura, String username) {
        log.info("Iniciando registro de compra. Factura: {}", request.getNroFactura());

        // 1. Obtener usuario responsable
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // 2. Guardar el archivo físico de la factura
        String rutaArchivo = null;
        if (archivoFactura != null && !archivoFactura.isEmpty()) {
            rutaArchivo = fileStorageService.guardarFactura(archivoFactura);
        }

        // 3. Preparar la entidad Compra (Cabecera)
        Compra compra = Compra.builder()
                .nroFactura(request.getNroFactura())
                .fechaCompra(request.getFechaCompra())
                .proveedor(request.getProveedor())
                .rutaArchivoFactura(rutaArchivo)
                .usuarioRegistro(usuario)
                .build();

        // Obtener el estado por defecto para nuevo inventario (Asumiremos que el estado "EN_STOCK" es el ID 1)
        EstadoEpp estadoEnStock = estadoEppRepository.findByNombre("EN_STOCK")
                .orElseThrow(() -> new BusinessException("El estado EN_STOCK no está configurado en el sistema."));

        BigDecimal totalCompra = BigDecimal.ZERO;

        // 4. Procesar cada EPP de la lista (Detalles y Actualización de Stock)
        for (DetalleCompraRequestDTO itemDTO : request.getItems()) {

            // a) Buscar el EPP en el catálogo
            CatalogoEpp epp = catalogoEppRepository.findById(itemDTO.getEppId())
                    .orElseThrow(() -> new BusinessException("EPP no encontrado en catálogo con ID: " + itemDTO.getEppId()));

            // b) Crear detalle de compra
            BigDecimal subtotal = itemDTO.getPrecioUnitario().multiply(BigDecimal.valueOf(itemDTO.getCantidad()));
            totalCompra = totalCompra.add(subtotal);

            DetalleCompra detalle = DetalleCompra.builder()
                    .epp(epp)
                    .cantidad(itemDTO.getCantidad())
                    .precioUnitario(itemDTO.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();

            compra.addDetalle(detalle);

            // c) Trazabilidad al Inventario Central (MAGIA AQUÍ)
            // Buscamos si ya existe stock de este EPP, con este Lote (usaremos NroFactura como Lote por defecto) y Estado "EN_STOCK"
            Optional<InventarioCentral> inventarioOpt = inventarioCentralRepository.findByEppAndLoteAndEstado(epp, request.getNroFactura(), estadoEnStock);

            if (inventarioOpt.isPresent()) {
                // Si existe, SUMAMOS la cantidad comprada
                InventarioCentral inv = inventarioOpt.get();
                inv.setCantidadActual(inv.getCantidadActual() + itemDTO.getCantidad());
                // Actualizamos la fecha de modificación
                inv.setUltimaActualizacion(LocalDateTime.now());
                inventarioCentralRepository.save(inv);
                log.info("Stock actualizado en Inventario Central para EPP: {}", epp.getNombreEpp());
            } else {
                // Si es un lote nuevo o primer ingreso, CREAMOS el registro
                InventarioCentral nuevoInv = InventarioCentral.builder()
                        .epp(epp)
                        .estado(estadoEnStock)
                        .cantidadActual(itemDTO.getCantidad())
                        .cantidadMinima(10) // Valor por defecto sugerido
                        .cantidadMaxima(100) // Valor por defecto sugerido
                        .lote(request.getNroFactura())
                        .costoUnitario(BigDecimal.valueOf(itemDTO.getPrecioUnitario().doubleValue()))
                        .proveedor(request.getProveedor())
                        .ubicacionBodega("Almacén General") // Por defecto
                        .fechaAdquisicion(request.getFechaCompra())
                        // ---- LÍNEAS AÑADIDAS PARA SOLUCIONAR EL ERROR ----
                        .fechaCreacion(LocalDateTime.now())
                        .ultimaActualizacion(LocalDateTime.now())
                        // --------------------------------------------------
                        .build();
                inventarioCentralRepository.save(nuevoInv);
                log.info("Nuevo registro de Inventario Central creado para EPP: {}", epp.getNombreEpp());
            }
        }

        // 5. Asignar el total y guardar la compra (Guardará los detalles automáticamente por el CascadeType.ALL)
        compra.setMontoTotal(totalCompra);
        return compraRepository.save(compra);
    }
}