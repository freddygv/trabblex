package pt.fcup.exception;

public class SeederGenerationException extends Exception {
    public SeederGenerationException () {

    }

    public SeederGenerationException (String message) {
        super (message);
    }

    public SeederGenerationException (Throwable cause) {
        super (cause);
    }

    public SeederGenerationException (String message, Throwable cause) {
        super (message, cause);
    }
}
