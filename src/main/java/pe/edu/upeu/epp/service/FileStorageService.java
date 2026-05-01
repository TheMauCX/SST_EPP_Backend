package pe.edu.upeu.epp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Directorio donde se guardarán las facturas
    private final Path fileStorageLocation = Paths.get("uploads/facturas").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se subirán las facturas.", ex);
        }
    }

    public String guardarFactura(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Generar un nombre único para evitar sobreescritura de archivos con el mismo nombre
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            String newFileName = UUID.randomUUID().toString() + extension;

            // Copiar el archivo al directorio de destino
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "uploads/facturas/" + newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo " + file.getOriginalFilename() + ". Por favor intente de nuevo!", ex);
        }
    }
}