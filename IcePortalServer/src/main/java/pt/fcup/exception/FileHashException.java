package pt.fcup.exception;

public class FileHashException extends Exception {
    public FileHashException () {

    }

    public FileHashException (String message) {
        super (message);
    }

    public FileHashException (Throwable cause) {
        super (cause);
    }

    public FileHashException (String message, Throwable cause) {
        super (message, cause);
    }
}