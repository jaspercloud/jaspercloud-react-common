package io.jaspercloud.react;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ReactHttpOutputMessage implements HttpOutputMessage {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private final HttpHeaders httpHeaders = new HttpHeaders();

    public byte[] getBytes() {
        return outputStream.toByteArray();
    }

    @Override
    public OutputStream getBody() throws IOException {
        return outputStream;
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }
}
