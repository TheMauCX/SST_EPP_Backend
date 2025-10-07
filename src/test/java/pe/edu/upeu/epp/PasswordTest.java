package pe.edu.upeu.epp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.epp.entity.Usuario;
import pe.edu.upeu.epp.repository.UsuarioRepository;

@SpringBootTest
public class PasswordTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordMatch() {
        String username = "admin";
        String passwordToTest = "Admin123!";

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("====================================");
        System.out.println("Usuario: " + usuario.getNombreUsuario());
        System.out.println("Hash en BD: " + usuario.getContrasenaHash());
        System.out.println("Password a probar: " + passwordToTest);

        boolean matches = passwordEncoder.matches(passwordToTest, usuario.getContrasenaHash());

        System.out.println("¿Coincide?: " + (matches ? "✅ SÍ" : "❌ NO"));
        System.out.println("====================================");

        if (!matches) {
            // Generar hash correcto
            String newHash = passwordEncoder.encode(passwordToTest);
            System.out.println("Hash correcto debería ser: " + newHash);
        }
    }
}