package co.edu.uptc.Exceptions;

/**
 * Excepción personalizada que se lanza cuando se intenta agregar un usuario que ya existe en el sistema.
 * @author @FELPBEC
 * @since 2026-19-11
 * @version 1.0
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }

}
