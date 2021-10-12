package io.jaspercloud.react.http.client;

import io.netty.handler.codec.http.HttpHeaders;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProcessHeader implements Consumer<HttpHeaders> {

    private Request request;

    public ProcessHeader(Request request) {
        this.request = request;
    }

    @Override
    public void accept(HttpHeaders entries) {
        try {
            Map<String, List<String>> map = request.headers().toMultimap();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                entries.add(entry.getKey(), entry.getValue());
            }
            RequestBody requestBody = request.body();
            if (null == requestBody) {
                return;
            }
            MediaType mediaType = requestBody.contentType();
            if (null != mediaType) {
                entries.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, mediaType.toString());
            }
            long contentLength = requestBody.contentLength();
            if (contentLength > 0) {
                entries.add(org.springframework.http.HttpHeaders.CONTENT_LENGTH, Long.toString(contentLength));
                entries.remove(org.springframework.http.HttpHeaders.TRANSFER_ENCODING);
            } else {
                entries.add(org.springframework.http.HttpHeaders.TRANSFER_ENCODING, "chunked");
                entries.remove(org.springframework.http.HttpHeaders.CONTENT_LENGTH);
            }
        } catch (Exception e) {
            throw new HttpProcessException(e.getMessage(), e);
        }
    }
}
