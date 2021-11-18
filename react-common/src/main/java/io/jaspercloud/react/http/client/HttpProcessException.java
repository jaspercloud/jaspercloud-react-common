package io.jaspercloud.react.http.client;

public class HttpProcessException extends RuntimeException {

    public HttpProcessException() {
    }

    public HttpProcessException(String message) {
        super(message);
    }

    public HttpProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpProcessException(Throwable cause) {
        super(cause);
    }
}
