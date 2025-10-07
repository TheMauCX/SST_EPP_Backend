package pe.edu.upeu.epp.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.entity.Usuario;
import pe.edu.upeu.epp.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementación de UserDetailsService para Spring Security.
 * Carga los detalles del usuario desde la base de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario: {}", username);

        // Buscar usuario con sus roles (usando el query optimizado)
        Usuario usuario = usuarioRepository.findByNombreUsuarioWithRoles(username)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        // Validar que el usuario esté activo
        if (!usuario.getActivo()) {
            log.warn("Intento de login con usuario inactivo: {}", username);
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // Validar si el usuario está bloqueado
        if (usuario.getBloqueadoHasta() != null &&
                usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            log.warn("Intento de login con usuario bloqueado: {}", username);
            throw new UsernameNotFoundException("Usuario bloqueado temporalmente");
        }

        // Convertir roles a authorities de Spring Security
        Collection<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol()))
                .collect(Collectors.toList());

        log.debug("Usuario {} cargado con {} roles", username, authorities.size());

        // IMPORTANTE: Retornar el User de Spring Security con la contraseña hasheada de la BD
        return User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getContrasenaHash())  // ⚠️ CRÍTICO: usar el hash de la BD
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(usuario.getBloqueadoHasta() != null &&
                        usuario.getBloqueadoHasta().isAfter(LocalDateTime.now()))
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }
}