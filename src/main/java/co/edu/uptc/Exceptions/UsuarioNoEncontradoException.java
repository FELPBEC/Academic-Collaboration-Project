package co.edu.uptc.Exceptions;

/**
 * Excepción personalizada que se lanza cuando un usuario no es encontrado en el sistema.
 * @author @FELPBEC
 * @since 2026-19-11
 * @version 1.0
 */
public class UsuarioNoEncontradoException extends Exception {
    public UsuarioNoEncontradoException(String message) {
        super(message);
    }

}
