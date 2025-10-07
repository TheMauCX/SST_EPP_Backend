package pe.edu.upeu.epp.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private String tipo = "Bearer";
    private Integer usuarioId;
    private String nombreUsuario;
    private String email;
    private Set<String> roles;
    private Integer trabajadorId;
    private String nombreCompleto;
    private Integer areaId;
    private String areaNombre;
}