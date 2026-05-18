package pe.edu.upeu.epp.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.PublicAccessType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.epp.exception.BusinessException;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Service
public class AzureStorageService {

    private final BlobContainerClient blobContainerClient;

    public AzureStorageService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName) {

        // Inicializa el cliente principal de Azure
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Obtiene o crea el contenedor de archivos
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        if (!blobContainerClient.exists()) {
            blobContainerClient.create();

            // Configura el contenedor para que los archivos sean legibles públicamente mediante URL
            blobContainerClient.setAccessPolicy(PublicAccessType.BLOB, null);
        }
    }

    /**
     * Sube un archivo a Azure Blob Storage y retorna su URL pública.
     */
    public String subirArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            String uniqueBlobName = UUID.randomUUID().toString() + extension;

            BlobClient blobClient = blobContainerClient.getBlobClient(uniqueBlobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            return blobClient.getBlobUrl();

        } catch (IOException ex) {
            // Imprimimos el error completo en la consola
            ex.printStackTrace();
            throw new BusinessException("Error al leer el archivo físico: " + file.getOriginalFilename());
        } catch (Exception ex) {
            // Imprimimos el error completo que manda Azure en la consola
            ex.printStackTrace();
            throw new BusinessException("Error de Azure Blob Storage: " + ex.getMessage());
        }
    }
}