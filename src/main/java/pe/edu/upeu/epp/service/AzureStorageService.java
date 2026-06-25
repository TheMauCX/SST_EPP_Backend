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
 * Servicio de almacenamiento Azure Blob.
 *
 * Carpetas organizadas:
 *   epps/fotos/       → fotos del catálogo
 *   epps/fichas/      → PDFs de fichas técnicas
 *   compras/facturas/ → comprobantes de compra
 *   compras/cotizaciones/ → cotizaciones (sprint 4b, opcional)
 *   misc/             → uso legacy/genérico
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

    public String subirFotoEpp(MultipartFile file, Integer eppId) {
        String blobName = String.format("epps/fotos/epp-%d-foto-%d-%s%s",
                eppId, ts(), uuid(), ext(file));
        return subir(file, blobName);
    }

    public String subirFichaTecnica(MultipartFile file, Integer eppId) {
        validarPdf(file);
        String blobName = String.format("epps/fichas/epp-%d-ficha-%d-%s.pdf",
                eppId, ts(), uuid());
        return subir(file, blobName);
    }

    public String subirFactura(MultipartFile file, String nroFactura) {
        String nro = nroFactura.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String blobName = String.format("compras/facturas/%s-%d-%s%s", nro, ts(), uuid(), ext(file));
        return subir(file, blobName);
    }

    /**
     * Sube el archivo de cotización vinculado a una compra.
     * Ruta: compras/cotizaciones/{nroFactura}-cotizacion-{ts}.{ext}
     * Sprint 4b — opcional.
     */
    public String subirCotizacion(MultipartFile file, String nroFactura) {
        if (file == null || file.isEmpty()) return null;
        String nro = nroFactura.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String blobName = String.format("compras/cotizaciones/%s-cotizacion-%d-%s%s",
                nro, ts(), uuid(), ext(file));
        return subir(file, blobName);
    }

    /** @deprecated Usar métodos tipados. Sube a misc/. */
    @Deprecated
    public String subirArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        return subir(file, "misc/" + UUID.randomUUID() + ext(file));
    }

    // ── Implementación interna ────────────────────────────────────────────────

    private String subir(MultipartFile file, String blobName) {
        if (file == null || file.isEmpty()) return null;
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            log.info("Azure upload OK: {}", blobName);
            return blobClient.getBlobUrl();
        } catch (IOException ex) {
            log.error("Error leyendo archivo: {}", file.getOriginalFilename(), ex);
            throw new BusinessException("Error al leer el archivo: " + file.getOriginalFilename());
        } catch (Exception ex) {
            log.error("Error Azure upload {}: {}", blobName, ex.getMessage(), ex);
            throw new BusinessException("Error de Azure Blob Storage: " + ex.getMessage());
        }
    }

    private void validarPdf(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new BusinessException("El archivo PDF no puede estar vacío.");
        String nombre = file.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".pdf"))
            throw new BusinessException("Solo se aceptan archivos PDF para fichas técnicas.");
        if (file.getSize() > 5 * 1024 * 1024)
            throw new BusinessException("El PDF no puede superar 5 MB.");
    }

    private String ext(MultipartFile file) {
        String n = file.getOriginalFilename();
        if (n != null && n.contains(".")) return n.substring(n.lastIndexOf(".")).toLowerCase();
        return "";
    }

    private long ts()     { return Instant.now().toEpochMilli(); }
    private String uuid() { return UUID.randomUUID().toString().substring(0, 8); }
}
