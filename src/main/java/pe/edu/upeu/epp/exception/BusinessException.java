package pe.edu.upeu.epp.exception;
/**

 Excepción de negocio personalizada.
 Se lanza cuando ocurren errores de lógica de negocio.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}