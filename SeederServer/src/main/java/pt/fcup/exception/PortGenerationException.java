package pt.fcup.exception;

public class PortGenerationException extends Exception {
    public PortGenerationException() {

    }

    public PortGenerationException(String message) {
        super (message);
    }

    public PortGenerationException(Throwable cause) {
        super (cause);
    }

    public PortGenerationException(String message, Throwable cause) {
        super (message, cause);
    }
}