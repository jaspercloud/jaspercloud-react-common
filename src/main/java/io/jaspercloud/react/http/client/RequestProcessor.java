package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class RequestProcessor implements ReactAsyncCall<Channel, FullHttpResponse> {

    private static Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    private Request request;

    public RequestProcessor(Request request) {
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
            CompositeByteBuf content = channel.alloc().compositeBuffer();
            RequestBody requestBody = request.body();
            if (null != requestBody) {
                BufferedSink buffer = Okio.buffer(Okio.sink(new OutputStream() {
                    @Override
                    public void write(byte[] b) throws IOException {
                        ByteBuf buf = channel.alloc().buffer(b.length);
                        buf.writeBytes(b);
                        content.addComponent(buf);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        byte[] bytes = Arrays.copyOfRange(b, off, len);
                        ByteBuf buf = channel.alloc().buffer(bytes.length);
                        buf.writeBytes(bytes);
                        content.addComponent(buf);
                    }

                    @Override
                    public void write(int b) throws IOException {
                        ByteBuf buf = channel.alloc().buffer(1);
                        buf.writeByte((byte) b);
                        content.addComponent(buf);
                    }
                }));
                requestBody.writeTo(buffer);
            }
            DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(httpVersion, method, uri, content);
            request.headers().toMultimap().entrySet().forEach(e -> {
                String key = e.getKey();
                List<String> values = e.getValue();
                values.forEach(val -> {
                    if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(val)) {
                        fullHttpRequest.headers().add(key, val);
                    }
                });
            });
            if (null == request.headers().get("Host")) {
                fullHttpRequest.headers().add("Host", Util.hostHeader(request.url(), false));
            }
            if (null == request.headers().get("Connection")) {
                fullHttpRequest.headers().add("Connection", "Keep-Alive");
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
            channel.writeAndFlush(fullHttpRequest);
        } catch (Throwable e) {
            sink.error(e);
        }
    }
}
