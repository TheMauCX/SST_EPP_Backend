package pe.edu.upeu.epp.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PublicAccessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.exception.BusinessException;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Servicio de almacenamiento en Azure Blob Storage.
 *
 * Sprint 4 — mejora organizativa:
 *   Los archivos ya no se suben al root del contenedor con un UUID genérico.
 *   Ahora se organizan en carpetas por módulo y con nombres descriptivos:
 *
 *   epps/fotos/     → fotos de referencia del catálogo EPP
 *   epps/fichas/    → PDFs de fichas técnicas
 *   compras/facturas/ → comprobantes de compra
 *
 *   Formato del nombre: {prefijo}-{timestamp}-{uuid_corto}.{ext}
 *   Ejemplo: epp-12-ficha-1748000000000-a1b2.pdf
 */
@Service
@Slf4j
public class AzureStorageService {

    private final BlobContainerClient blobContainerClient;

    public AzureStorageService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName) {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
            blobContainerClient.setAccessPolicy(PublicAccessType.BLOB, null);
        }
    }

    // ── API pública por tipo de archivo ──────────────────────────────────

    /**
     * Sube la foto de referencia de un EPP del catálogo.
     * Ruta: epps/fotos/epp-{eppId}-foto-{ts}.{ext}
     */
    public String subirFotoEpp(MultipartFile file, Integer eppId) {
        String blobName = String.format("epps/fotos/epp-%d-foto-%d-%s%s",
                eppId, Instant.now().toEpochMilli(), uuidCorto(), extension(file));
        return subir(file, blobName);
    }

    /**
     * Sube el PDF de ficha técnica de un EPP.
     * Ruta: epps/fichas/epp-{eppId}-ficha-{ts}.pdf
     * HU-19
     */
    public String subirFichaTecnica(MultipartFile file, Integer eppId) {
        validarPdf(file);
        String blobName = String.format("epps/fichas/epp-%d-ficha-%d-%s.pdf",
                eppId, Instant.now().toEpochMilli(), uuidCorto());
        return subir(file, blobName);
    }

    /**
     * Sube el comprobante/factura de una compra.
     * Ruta: compras/facturas/{nroFactura}-{ts}.{ext}
     */
    public String subirFactura(MultipartFile file, String nroFactura) {
        // Sanitizar el número de factura para usarlo como parte del nombre de blob
        String nroSanitizado = nroFactura.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String blobName = String.format("compras/facturas/%s-%d-%s%s",
                nroSanitizado, Instant.now().toEpochMilli(), uuidCorto(), extension(file));
        return subir(file, blobName);
    }

    /**
     * Método genérico — se mantiene para compatibilidad con código existente
     * que llame a subirArchivo() directamente. Sube a la carpeta 'misc/'.
     *
     * @deprecated Usar subirFotoEpp, subirFichaTecnica o subirFactura según el tipo.
     */
    @Deprecated
    public String subirArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        String blobName = "misc/" + UUID.randomUUID() + extension(file);
        return subir(file, blobName);
    }

    // ── Implementación interna ───────────────────────────────────────────

    private String subir(MultipartFile file, String blobName) {
        if (file == null || file.isEmpty()) return null;
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            log.info("Archivo subido a Azure: {}", blobName);
            return blobClient.getBlobUrl();
        } catch (IOException ex) {
            log.error("Error leyendo archivo para subir a Azure: {}", file.getOriginalFilename(), ex);
            throw new BusinessException("Error al leer el archivo: " + file.getOriginalFilename());
        } catch (Exception ex) {
            log.error("Error de Azure Blob Storage al subir {}: {}", blobName, ex.getMessage(), ex);
            throw new BusinessException("Error de Azure Blob Storage: " + ex.getMessage());
        }
    }

    private void validarPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo PDF no puede estar vacío.");
        }
        String nombre = file.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException("Solo se aceptan archivos PDF para fichas técnicas.");
        }
        // 5 MB máximo
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("El PDF no puede superar 5 MB.");
        }
    }

    private String extension(MultipartFile file) {
        String nombre = file.getOriginalFilename();
        if (nombre != null && nombre.contains(".")) {
            return nombre.substring(nombre.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }

    private String uuidCorto() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
