package pe.edu.upeu.epp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.epp.dto.request.ForgotPasswordRequestDTO;
import pe.edu.upeu.epp.dto.request.LoginRequestDTO;
import pe.edu.upeu.epp.dto.request.RefreshTokenRequestDTO;
import pe.edu.upeu.epp.dto.request.ResetPasswordRequestDTO;
import pe.edu.upeu.epp.dto.response.AuthResponseDTO;
import pe.edu.upeu.epp.entity.Usuario;
import pe.edu.upeu.epp.exception.BusinessException;
import pe.edu.upeu.epp.repository.UsuarioRepository;
import pe.edu.upeu.epp.security.JwtService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación.
 *
 * Sprint 4b: usuario ya no está ligado a trabajador.
 * AuthResponseDTO simplificado: token, usuarioId, nombreUsuario, email,
 * nombreCompleto, roles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Login: {}", request.getNombreUsuario());

        Usuario usuario = usuarioRepository.findByNombreUsuario(request.getNombreUsuario())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!usuario.getActivo())
            throw new BusinessException("Usuario inactivo. Contacte al administrador.");

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now()))
            throw new BusinessException("Usuario bloqueado temporalmente. Intente más tarde.");

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getNombreUsuario(), request.getContrasena()));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);

            return buildResponse(usuario,
                    jwtService.generateToken(userDetails),
                    jwtService.generateRefreshToken(userDetails));

        } catch (BadCredentialsException e) {
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= 5) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
                usuarioRepository.save(usuario);
                throw new BusinessException("Usuario bloqueado por múltiples intentos fallidos.");
            }
            usuarioRepository.save(usuario);
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String username = jwtService.extractUsername(request.getRefreshToken());
        Usuario usuario = usuarioRepository.findByNombreUsuarioWithRoles(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!usuario.getActivo()) throw new BusinessException("Usuario inactivo");

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getContrasenaHash())
                .authorities(usuario.getRoles().stream()
                        .map(r -> "ROLE_" + r.getNombreRol()).toArray(String[]::new))
                .build();

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails))
            throw new BusinessException("Refresh token inválido o expirado");

        return buildResponse(usuario, jwtService.generateToken(userDetails), request.getRefreshToken());
    }

    @Transactional
    public void logout(String username) {
        usuarioRepository.findByNombreUsuario(username).ifPresent(u -> {
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        String input = request.getUsernameOrEmail();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(input);
        if (usuarioOpt.isEmpty()) usuarioOpt = usuarioRepository.findByEmail(input);

        usuarioOpt.ifPresent(u -> {
            String destino = u.getEmail();
            if (destino != null && !destino.isBlank()) {
                String token = UUID.randomUUID().toString();
                u.setResetToken(token);
                u.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
                usuarioRepository.save(u);
                emailService.sendPasswordResetEmail(destino, token);
            }
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new BusinessException("Las contraseñas no coinciden");

        Usuario usuario = usuarioRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Token inválido"));

        if (usuario.getResetTokenExpiry() == null
                || usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            usuario.setResetToken(null);
            usuario.setResetTokenExpiry(null);
            usuarioRepository.save(usuario);
            throw new BusinessException("Token expirado");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(request.getNewPassword()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepository.save(usuario);
    }

    private AuthResponseDTO buildResponse(Usuario usuario, String token, String refreshToken) {
        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .usuarioId(usuario.getUsuarioId())
                .nombreUsuario(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .roles(usuario.getRoles().stream()
                        .map(r -> r.getNombreRol())
                        .collect(Collectors.toSet()))
                .build();
    }
}
