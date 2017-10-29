package pt.fcup.exception;

/*
    Note: copied from seedbox - quick and dirty :(
*/

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