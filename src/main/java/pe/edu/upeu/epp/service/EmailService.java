package pe.edu.upeu.epp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de "correo" simulado para el entorno de desarrollo.
 * No envía correos reales; en su lugar, imprime el enlace de reseteo
 * en la consola para facilitar las pruebas.
 */
@Service
public class EmailService {

    // Usamos un Logger para imprimir en la consola
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Simula el envío del correo para restablecer la contraseña.
     * @param to Correo del usuario (se loggea para info)
     * @param token Token de reseteo
     */
    public void sendPasswordResetEmail(String to, String token) {
        // ¡Importante! La URL debe apuntar a tu APLICACIÓN FRONTEND.
        String frontendUrl = "http://localhost:4200/reset-password"; // Cambia esto por la URL de tu frontend
        String resetLink = frontendUrl + "?token=" + token;

        String subject = "Restablecimiento de Contraseña - Gestión EPP";

        // --- SIMULACIÓN DE CORREO ---
        // En lugar de enviar un correo, loggeamos la información crítica
        // para que el desarrollador pueda probar el flujo.
        logger.info("************************************************************");
        logger.info("--- SIMULACIÓN DE ENVÍO DE CORREO (ENTORNO DEV) ---");
        logger.info("Para:      {}", to);
        logger.info("Asunto:    {}", subject);
        logger.info("Enlace de Reseteo (Copiar y pegar en el navegador):");
        logger.info("{}", resetLink);
        logger.info("************************************************************");
    }
}