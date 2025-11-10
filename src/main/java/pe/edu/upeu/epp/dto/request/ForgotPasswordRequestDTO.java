package pe.edu.upeu.epp.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequestDTO {
    @NotBlank(message = "El nombre de usuario o correo no puede estar vacío")
    private String usernameOrEmail; // El usuario puede ingresar su username o su email
}