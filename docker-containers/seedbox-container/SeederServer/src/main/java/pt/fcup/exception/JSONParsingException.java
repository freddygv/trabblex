package pt.fcup.exception;

public class JSONParsingException extends Exception {
    public JSONParsingException () {

    }

    public JSONParsingException (String message) {
        super (message);
    }

    public JSONParsingException (Throwable cause) {
        super (cause);
    }

    public JSONParsingException (String message, Throwable cause) {
        super (message, cause);
    }
}