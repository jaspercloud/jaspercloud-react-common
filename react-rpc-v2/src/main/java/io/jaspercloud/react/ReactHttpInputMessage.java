package io.jaspercloud.react;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.IOException;
import java.io.InputStream;

public class ReactHttpInputMessage implements HttpInputMessage {

    private InputStream stream;
    private HttpHeaders httpHeaders = new HttpHeaders();

    public ReactHttpInputMessage(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public InputStream getBody() throws IOException {
        return stream;
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }
}
