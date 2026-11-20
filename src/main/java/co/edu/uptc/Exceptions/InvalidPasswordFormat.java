package co.edu.uptc.Exceptions;

public class InvalidPasswordFormat extends RuntimeException {
    public InvalidPasswordFormat(String message) {
        super(message);
    }
    
}
