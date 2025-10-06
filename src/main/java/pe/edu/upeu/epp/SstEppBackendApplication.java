package pe.edu.upeu.epp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Plataforma de Gestión de Equipos de Protección Personal (EPP) - UPEU
 *
 * Sistema empresarial para digitalizar el ciclo completo de gestión de EPP,
 * desde la compra hasta la baja, asegurando la integridad del inventario.
 *
 * @version 1.0.0
 * @author UPEU Development Team
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
public class SstEppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SstEppBackendApplication.class, args);
    }
}