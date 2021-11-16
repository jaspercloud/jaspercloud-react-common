package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.netty.handler.codec.http.FullHttpResponse;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;

public class ResponseProcessor implements ReactAsyncCall<FullHttpResponse, Response> {

    private Request request;

    public ResponseProcessor(Request request) {
        this.request = request;
    }

    @Override
    public void process(boolean hasError, Throwable throwable, FullHttpResponse fullHttpResponse, ReactSink<? super Response> sink) throws Throwable {
        if (hasError) {
            throw throwable;
        }
        //header
        Headers.Builder headersBuilder = new Headers.Builder();
        fullHttpResponse.headers().forEach(e -> {
            headersBuilder.add(e.getKey(), e.getValue());
        });
        Headers headers = headersBuilder.build();
        //response
        Response.Builder responseBuilder = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(fullHttpResponse.status().code())
                .message(fullHttpResponse.status().reasonPhrase())
                .headers(headers);
        String contentType = headers.get(HttpHeaders.CONTENT_TYPE);
        MediaType mediaType = StringUtils.isNotEmpty(contentType) ? MediaType.parse(contentType) : null;
        int contentLength = NumberUtils.toInt(headers.get(HttpHeaders.CONTENT_LENGTH), -1);
        int readableBytes = contentLength > 0 ? contentLength : fullHttpResponse.content().readableBytes();
        byte[] bytes = new byte[readableBytes];
        fullHttpResponse.content().readBytes(bytes);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ResponseBody responseBody = ResponseBody.create(mediaType, contentLength, Okio.buffer(Okio.source(inputStream)));
        responseBuilder.body(responseBody);
        sink.success(responseBuilder.build());
    }
}
