package io.jaspercloud.react.exception;

public class ReactException extends RuntimeException {

    public ReactException() {
    }

    public ReactException(String message) {
        super(message);
    }

    public ReactException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReactException(Throwable cause) {
        super(cause);
    }
}
