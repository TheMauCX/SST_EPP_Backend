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
import pe.edu.upeu.epp.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
/**

 Configuración de seguridad de Spring Security.
 Define la cadena de filtros, autenticación JWT, CORS y autorizaciones por rol.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    /**

     Configura la cadena de filtros de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
// Deshabilitar CSRF (no necesario para APIs stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)
                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configurar autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Endpoints de administración (solo ADMINISTRADOR_SISTEMA)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMINISTRADOR_SISTEMA")
                        .requestMatchers(HttpMethod.POST, "/api/v1/catalogo-epp/**").hasRole("ADMINISTRADOR_SISTEMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/catalogo-epp/**").hasRole("ADMINISTRADOR_SISTEMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/catalogo-epp/**").hasRole("ADMINISTRADOR_SISTEMA")

                        // Endpoints de gestión de inventario central
                        .requestMatchers("/api/v1/inventario-central/**").hasAnyRole("SUPERVISOR_SST", "ADMINISTRADOR_SISTEMA")

                        // Endpoints de entregas (JEFE_AREA y SUPERVISOR_SST)
                        .requestMatchers(HttpMethod.POST, "/api/v1/entregas/**").hasAnyRole("JEFE_AREA", "SUPERVISOR_SST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/entregas/**").hasAnyRole("JEFE_AREA", "SUPERVISOR_SST", "COORDINADOR_SST")

                        // Endpoints de solicitudes de reposición
                        .requestMatchers(HttpMethod.POST, "/api/v1/solicitudes-reposicion").hasRole("JEFE_AREA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes-reposicion/*/aprobar").hasRole("SUPERVISOR_SST")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes-reposicion/*/rechazar").hasRole("SUPERVISOR_SST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes-reposicion/**").hasAnyRole("JEFE_AREA", "SUPERVISOR_SST", "COORDINADOR_SST")

                        // Endpoints de reportes (accesibles para roles con permisos de lectura)
                        .requestMatchers("/api/v1/reportes/**").hasAnyRole("SUPERVISOR_SST", "COORDINADOR_SST", "ADMINISTRADOR_SISTEMA")

                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )

                // Configurar gestión de sesiones (stateless para JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Agregar el filtro JWT antes del filtro de autenticación de usuario/contraseña
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**

     Proveedor de autenticación que usa UserDetailsService y PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**

     AuthenticationManager para autenticación manual (usado en login).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**

     Encoder de contraseñas BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**

     Configuración de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
// Orígenes permitidos (ajustar según ambiente)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5354",
                "https://app.upeu.edu.pe",
                "http://localhost:59927",
                "http://10.0.2.2:8080"
        ));
// Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
// Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));
// Permitir credenciales (cookies, headers de autorización)
        configuration.setAllowCredentials(true);
// Tiempo de cache de la configuración CORS (1 hora)
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
