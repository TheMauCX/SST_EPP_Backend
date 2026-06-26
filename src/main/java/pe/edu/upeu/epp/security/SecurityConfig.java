package pe.edu.upeu.epp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad.
 *
 * Sprint 4b:
 *   El producto es de uso exclusivo para personal SST.
 *   Los roles activos son: SUPERVISOR_SST (principal) y ADMINISTRADOR_SISTEMA (legacy/compatibilidad).
 *   Todos los endpoints de gestión son accesibles para ambos roles sin distinción,
 *   ya que el supervisor SST tiene acceso completo al sistema.
 *
 *   Roles eliminados del alcance MVP: JEFE_AREA, COORDINADOR_SST.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Orígenes CORS permitidos, separados por coma.
     * En desarrollo local se usa el valor por defecto.
     * En Azure Container Apps se inyecta la variable ALLOWED_ORIGINS con las URLs reales.
     * Ejemplo: https://sst-epp-frontend.azurecontainerapps.io,https://app.upeu.edu.pe
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5354,http://localhost:5000,http://localhost:4200,http://10.0.2.2:8080,http://172.17.25.28:8080}")
    private String allowedOrigins;

    // Roles activos en el MVP
    private static final String SUPERVISOR = "SUPERVISOR_SST";
    private static final String ADMIN      = "ADMINISTRADOR_SISTEMA";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth

                // ── Público ─────────────────────────────────────────────
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/test-azure/**").permitAll()
                .requestMatchers("/api/v1/reportes/trabajadores/*/ficha").permitAll()
                .requestMatchers("/api/v1/reportes/trabajadores/*/ficha/pdf").permitAll()

                // ── Todo lo demás: requiere SUPERVISOR_SST o ADMINISTRADOR_SISTEMA ─
                // Se usa una sola regla global para evitar inconsistencias.
                // El @PreAuthorize en cada método puede añadir restricciones adicionales si fuera necesario.
                .anyRequest().hasAnyRole(SUPERVISOR, ADMIN)
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parsear la lista de orígenes separada por coma (inyectada por env var ALLOWED_ORIGINS)
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(Arrays.asList("Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
