package io.jaspercloud.react.exception;

public class ReactRpcException extends RuntimeException {

    public ReactRpcException() {
    }

    public ReactRpcException(String message) {
        super(message);
    }

    public ReactRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReactRpcException(Throwable cause) {
        super(cause);
    }
}
