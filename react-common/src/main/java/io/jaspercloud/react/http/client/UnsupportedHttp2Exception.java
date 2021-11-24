package io.jaspercloud.react.http.client;

public class UnsupportedHttp2Exception extends UnsupportedOperationException {

    public UnsupportedHttp2Exception() {
    }

    public UnsupportedHttp2Exception(String message) {
        super(message);
    }

    public UnsupportedHttp2Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedHttp2Exception(Throwable cause) {
        super(cause);
    }
}
