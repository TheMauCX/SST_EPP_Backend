package pe.edu.upeu.epp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes BCrypt de contraseñas.
 * Ejecutar como aplicación Java independiente o como test.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Contraseña a hashear
        String password = "Super123!";

        // Generar hash
        String hash = encoder.encode(password);

        System.out.println("=============================================");
        System.out.println("Generador de Hash BCrypt");
        System.out.println("=============================================");
        System.out.println("Password:       " + password);
        System.out.println("Hash generado:  " + hash);
        System.out.println("Longitud hash:  " + hash.length() + " caracteres");
        System.out.println("=============================================");

        // Verificar que el hash funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verificación:   " + (matches ? "✅ CORRECTO" : "❌ ERROR"));
        System.out.println("=============================================");

        // Query SQL para actualizar
        System.out.println("\nQuery SQL para actualizar:");
        System.out.println("UPDATE epp.usuario");
        System.out.println("SET contrasena_hash = '" + hash + "'");
        System.out.println("WHERE nombre_usuario = 'admin';");
        System.out.println("=============================================");
    }
}