package pe.edu.upeu.epp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de correo — STUB / NO IMPLEMENTADO.
 *
 * TODO (sprint futuro): Reemplazar con envío real usando JavaMailSender.
 *   - Leer frontendUrl desde variable de entorno FRONTEND_URL
 *   - Usar una plantilla HTML Thymeleaf para el email
 *   - Configurar MAIL_USERNAME y MAIL_PASSWORD en Azure Container Apps
 *
 * Por ahora solo loguea que se solicitó el reset (sin exponer el token).
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Stub: simula el envío del correo de restablecimiento de contraseña.
     * No envía ningún email real.
     *
     * @param to    Correo del usuario destinatario
     * @param token Token de reseteo (no se loguea por seguridad)
     */
    public void sendPasswordResetEmail(String to, String token) {
        // NOTA: El token NO se loguea para evitar exposición en logs de producción (Azure Monitor).
        // TODO (sprint futuro): implementar envío real con JavaMailSender.
        logger.info("[EmailService] Solicitud de reset de contraseña para: {} — email no enviado (stub).", to);
    }
}