package pe.edu.upeu.epp.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.request.LoginRequestDTO;
import pe.edu.upeu.epp.dto.request.RefreshTokenRequestDTO;
import pe.edu.upeu.epp.dto.response.AuthResponseDTO;
import pe.edu.upeu.epp.service.AuthService;
/**

 Controller de autenticación.
 Endpoints públicos para login, refresh token y logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación y gestión de tokens")
public class AuthController {
    private final AuthService authService;
    /**

     Endpoint de login.

     @param request Credenciales del usuario
     @return Tokens JWT y datos del usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna tokens JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**

     Endpoint para refrescar el access token.

     @param request Refresh token
     @return Nuevo access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Genera un nuevo access token usando el refresh token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**

     Endpoint de logout.
     Nota: En JWT, el logout se maneja principalmente en el cliente eliminando los tokens.
     Este endpoint es opcional y puede usarse para registro de auditoría.
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario actual")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            authService.logout(authentication.getName());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}