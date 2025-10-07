package pe.edu.upeu.epp.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.LoginRequestDTO;
import pe.edu.upeu.epp.dto.request.RefreshTokenRequestDTO;
import pe.edu.upeu.epp.dto.response.AuthResponseDTO;
import pe.edu.upeu.epp.entity.Usuario;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.UsuarioRepository;
import pe.edu.upeu.epp.security.JwtService;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
/**

 Servicio de autenticación.
 Maneja login, refresh token y logout.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    /**

     Autentica un usuario y genera tokens JWT.
     */
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {

        log.info("Intento de login para usuario: {}", request.getNombreUsuario());
// Buscar usuario
        Usuario usuario = usuarioRepository.findByNombreUsuario(request.getNombreUsuario())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
// Verificar si el usuario está activo
        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario inactivo. Contacte al administrador.");
        }
// Verificar si el usuario está bloqueado
        if (usuario.getBloqueadoHasta() != null &&
                usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Usuario bloqueado temporalmente. Intente más tarde.");
        }
        try {
// Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getNombreUsuario(),
                            request.getContrasena()
                    )
            );
            // Obtener UserDetails del resultado de autenticación
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Resetear intentos fallidos
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);

            // Generar tokens
            String token = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("Login exitoso para usuario: {}", request.getNombreUsuario());

            // Construir respuesta
            return buildAuthResponse(usuario, token, refreshToken);
        } catch (BadCredentialsException e) {
// Incrementar intentos fallidos
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            // Bloquear después de 5 intentos fallidos (por 15 minutos)
            if (usuario.getIntentosFallidos() >= 5) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
                usuarioRepository.save(usuario);
                throw new BusinessException("Usuario bloqueado por múltiples intentos fallidos. Intente en 15 minutos.");
            }

            usuarioRepository.save(usuario);
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    /**

     Refresca el token JWT.
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        log.info("Solicitud de refresh token");
// Extraer username del refresh token
        String username = jwtService.extractUsername(request.getRefreshToken());
// Buscar usuario
        Usuario usuario = usuarioRepository.findByNombreUsuarioWithRoles(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
// Verificar si el usuario está activo
        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario inactivo");
        }
// Cargar UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getContrasenaHash())
                .authorities(usuario.getRoles().stream()
                        .map(rol -> "ROLE_" + rol.getNombreRol())
                        .toArray(String[]::new))
                .build();
// Validar el refresh token
        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new BusinessException("Refresh token inválido o expirado");
        }
// Generar nuevo access token
        String newToken = jwtService.generateToken(userDetails);
        log.info("Refresh token exitoso para usuario: {}", username);
// Construir respuesta (reutilizar el mismo refresh token)
        return buildAuthResponse(usuario, newToken, request.getRefreshToken());
    }

    /**

     Logout (invalidar token del lado del cliente).
     En JWT, el logout se maneja típicamente en el cliente eliminando el token.
     Opcionalmente, se puede implementar una lista negra de tokens en Redis.
     */
    @Transactional
    public void logout(String username) {
        log.info("Logout para usuario: {}", username);
// Actualizar último acceso
        usuarioRepository.findByNombreUsuario(username)
                .ifPresent(usuario -> {
                    usuario.setUltimoAcceso(LocalDateTime.now());
                    usuarioRepository.save(usuario);
                });
// TODO: Si se implementa lista negra de tokens, agregar el token aquí
    }

    /**

     Construye la respuesta de autenticación con todos los datos del usuario.
     */
    private AuthResponseDTO buildAuthResponse(Usuario usuario, String token, String refreshToken) {
        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .usuarioId(usuario.getUsuarioId())
                .nombreUsuario(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .roles(usuario.getRoles().stream()
                        .map(rol -> rol.getNombreRol())
                        .collect(Collectors.toSet()))
                .trabajadorId(usuario.getTrabajador() != null ? usuario.getTrabajador().getTrabajadorId() : null)
                .nombreCompleto(usuario.getTrabajador() != null ?
                        usuario.getTrabajador().getNombres() + " " + usuario.getTrabajador().getApellidos() : null)
                .areaId(usuario.getTrabajador() != null && usuario.getTrabajador().getArea() != null ?
                        usuario.getTrabajador().getArea().getAreaId() : null)
                .areaNombre(usuario.getTrabajador() != null && usuario.getTrabajador().getArea() != null ?
                        usuario.getTrabajador().getArea().getNombreArea() : null)
                .build();
    }
}