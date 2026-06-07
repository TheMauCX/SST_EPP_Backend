package pe.edu.upeu.epp.dto.response;

import lombok.*;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String refreshToken;
    private String tipo = "Bearer";

    private Integer usuarioId;
    private String  nombreUsuario;
    private String  email;
    private String  nombreCompleto;
    private Set<String> roles;
}
