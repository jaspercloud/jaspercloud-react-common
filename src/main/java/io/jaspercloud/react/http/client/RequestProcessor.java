package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class RequestProcessor implements ReactAsyncCall<Channel, FullHttpResponse> {

    private static Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    private HttpConfig httpConfig;
    private Request request;

    public RequestProcessor(HttpConfig httpConfig, Request request) {
        this.httpConfig = httpConfig;
        this.request = request;
    }

    @Override
    public void process(boolean hasError, Throwable throwable, Channel channel, ReactSink<? super FullHttpResponse> sink) throws Throwable {
        if (hasError) {
            throw throwable;
        }
        try {
            HttpVersion httpVersion = HttpVersion.HTTP_1_1;
            HttpMethod method = HttpMethod.valueOf(request.method());
            String uri = request.url().toString();
            HttpRequest httpRequest = new DefaultHttpRequest(httpVersion, method, uri);
            request.headers().toMultimap().entrySet().forEach(e -> {
                String key = e.getKey();
                List<String> values = e.getValue();
                values.forEach(val -> {
                    if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(val)) {
                        httpRequest.headers().add(key, val);
                    }
                });
            });
            if (null == request.headers().get(HttpHeaders.HOST)) {
                httpRequest.headers().add(HttpHeaders.HOST, Util.hostHeader(request.url(), false));
            }
            if (null == request.headers().get(HttpHeaders.CONNECTION)) {
                httpRequest.headers().add(HttpHeaders.CONNECTION, "Keep-Alive");
            }
            if (null == request.headers().get(HttpHeaders.USER_AGENT)) {
                httpRequest.headers().add(HttpHeaders.USER_AGENT, httpConfig.getUserAgent());
            }
            RequestBody requestBody = request.body();
            if (null != requestBody) {
                if (null != requestBody.contentType()) {
                    httpRequest.headers().add(HttpHeaders.CONTENT_TYPE, requestBody.contentType().toString());
                }
                if (requestBody.contentLength() > 0) {
                    httpRequest.headers().add(HttpHeaders.CONTENT_LENGTH, requestBody.contentLength());
                }
            }
            CompletableFuture<FullHttpResponse> future = new CompletableFuture<>();
            future.whenComplete(new BiConsumer<FullHttpResponse, Throwable>() {
                @Override
                public void accept(FullHttpResponse fullHttpResponse, Throwable throwable) {
                    if (null != throwable) {
                        sink.error(throwable);
                    } else {
                        sink.success(fullHttpResponse);
                    }
                }
            });
            AttributeKeys.future(channel).set(future);
            channel.writeAndFlush(httpRequest);
            //send body
            if (null != requestBody) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                BufferedSink bufferedSink = Okio.buffer(Okio.sink(stream));
                requestBody.writeTo(bufferedSink);
                bufferedSink.flush();
                ByteBuf buffer = channel.alloc().buffer(stream.size());
                buffer.writeBytes(stream.toByteArray());
                HttpContent content = new DefaultHttpContent(buffer);
                channel.writeAndFlush(content);
            }
            HttpContent content = new DefaultLastHttpContent();
            channel.writeAndFlush(content);
        } catch (Throwable e) {
            sink.error(e);
        }
    }
}
