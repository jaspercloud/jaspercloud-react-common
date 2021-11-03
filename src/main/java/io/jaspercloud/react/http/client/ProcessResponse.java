package io.jaspercloud.react.http.client;

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
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SignalType;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ProcessResponse implements BiFunction<HttpClientResponse, ByteBufMono, Mono<Response>> {

    private Request request;

    public ProcessResponse(Request request) {
        this.request = request;
    }

    @Override
    public Mono<Response> apply(HttpClientResponse httpClientResponse, ByteBufMono byteBufMono) {
        //header
        Headers.Builder headersBuilder = new Headers.Builder();
        httpClientResponse.responseHeaders().forEach(e -> {
            headersBuilder.add(e.getKey(), e.getValue());
        });
        Headers headers = headersBuilder.build();
        //response
        Response.Builder responseBuilder = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(httpClientResponse.status().code())
                .message(httpClientResponse.status().reasonPhrase())
                .headers(headers);
        //result
        Mono<Response> result = Mono.create(new Consumer<MonoSink<Response>>() {
            @Override
            public void accept(MonoSink<Response> sink) {
                byteBufMono.asInputStream().subscribe(new BaseSubscriber<InputStream>() {

                    private InputStream next;
                    private Throwable throwable;

                    @Override
                    protected void hookOnNext(InputStream next) {
                        this.next = next;
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        this.throwable = throwable;
                    }

                    @Override
                    protected void hookFinally(SignalType type) {
                        if (null != throwable) {
                            sink.error(throwable);
                            return;
                        }
                        String contentType = headers.get(HttpHeaders.CONTENT_TYPE);
                        MediaType mediaType = StringUtils.isNotEmpty(contentType) ? MediaType.parse(contentType) : null;
                        long contentLength = NumberUtils.toLong(headers.get(HttpHeaders.CONTENT_LENGTH), -1);
                        if (null != next) {
                            ResponseBody responseBody = ResponseBody.create(mediaType, contentLength, Okio.buffer(Okio.source(next)));
                            responseBuilder.body(responseBody);
                        } else {
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
                            ResponseBody responseBody = ResponseBody.create(mediaType, contentLength, Okio.buffer(Okio.source(inputStream)));
                            responseBuilder.body(responseBody);
                        }
                        sink.success(responseBuilder.build());
                    }
                });
            }
        });
        return result;
    }
}
