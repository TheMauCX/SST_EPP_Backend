package pe.edu.upeu.epp.security;

import lombok.RequiredArgsConstructor;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth

                        // ── Endpoints públicos ──────────────────────────────────
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/test-azure/**").permitAll()

                        // ── Catálogo EPP (escritura solo ADMIN) ─────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/v1/catalogo-epp/**").hasRole("ADMINISTRADOR_SISTEMA")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/catalogo-epp/**").hasRole("ADMINISTRADOR_SISTEMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/catalogo-epp/**").hasRole("ADMINISTRADOR_SISTEMA")

                        // ── Tallas (solo ADMIN) ─────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/v1/tallas/**").hasRole("ADMINISTRADOR_SISTEMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tallas/**").hasRole("ADMINISTRADOR_SISTEMA")

                        // ── Inventario central ──────────────────────────────────
                        .requestMatchers("/api/v1/inventario-central/**")
                        .hasAnyRole("SUPERVISOR_SST", "ADMINISTRADOR_SISTEMA")

                        // ── Entregas (ADMIN + SUPERVISOR_SST + JEFE_AREA) ───────
                        // FIX: ADMINISTRADOR_SISTEMA estaba ausente en esta regla,
                        // causando 403 aunque el @PreAuthorize del controller lo permitía.
                        // La regla del SecurityConfig se evalúa ANTES que @PreAuthorize.
                        .requestMatchers(HttpMethod.POST, "/api/v1/entregas/**")
                        .hasAnyRole("ADMINISTRADOR_SISTEMA", "SUPERVISOR_SST", "JEFE_AREA")
                        .requestMatchers(HttpMethod.GET,  "/api/v1/entregas/**")
                        .hasAnyRole("ADMINISTRADOR_SISTEMA", "SUPERVISOR_SST", "JEFE_AREA", "COORDINADOR_SST")

                        // ── Inventario área ─────────────────────────────────────
                        .requestMatchers("/api/v1/inventario-area/**")
                        .hasAnyRole("ADMINISTRADOR_SISTEMA", "SUPERVISOR_SST", "JEFE_AREA", "COORDINADOR_SST")

                        // ── Trabajadores ────────────────────────────────────────
                        .requestMatchers("/api/v1/trabajadores/**")
                        .hasAnyRole("ADMINISTRADOR_SISTEMA", "SUPERVISOR_SST", "JEFE_AREA", "COORDINADOR_SST")

                        // ── Áreas ───────────────────────────────────────────────
                        .requestMatchers("/api/v1/areas/**")
                        .hasAnyRole("ADMINISTRADOR_SISTEMA", "SUPERVISOR_SST", "COORDINADOR_SST")

                        // ── Compras ─────────────────────────────────────────────
                        .requestMatchers("/api/v1/compras/**")
                        .hasAnyRole("ADMINISTRADOR_SISTEMA", "SUPERVISOR_SST")

                        // ── Reportes ────────────────────────────────────────────
                        .requestMatchers("/api/v1/reportes/**")
                        .hasAnyRole("ADMINISTRADOR_SISTEMA", "SUPERVISOR_SST", "COORDINADOR_SST")

                        // ── Solicitudes de reposición (fuera de alcance MVP) ────
                        .requestMatchers(HttpMethod.POST, "/api/v1/solicitudes-reposicion")
                        .hasRole("JEFE_AREA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes-reposicion/*/aprobar")
                        .hasRole("SUPERVISOR_SST")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes-reposicion/*/rechazar")
                        .hasRole("SUPERVISOR_SST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes-reposicion/**")
                        .hasAnyRole("JEFE_AREA", "SUPERVISOR_SST", "COORDINADOR_SST")

                        // ── Todo lo demás requiere autenticación ─────────────────
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
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
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5354",
                "https://app.upeu.edu.pe",
                "http://localhost:5000",
                "http://10.0.2.2:8080",
                "http://172.17.25.28:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "PATCH", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}