package pe.edu.upeu.epp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // La configuración de auditoría está habilitada por @EnableJpaAuditing
}