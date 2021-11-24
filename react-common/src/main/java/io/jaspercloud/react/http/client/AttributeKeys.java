package io.jaspercloud.react.http.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class AttributeKeys {

    public static final int DefaultStreamId = 1;

    private AttributeKeys() {
    }

    public static Map<Integer, CompletableFuture<FullHttpResponse>> future(Channel channel) {
        Attribute<Map<Integer, CompletableFuture<FullHttpResponse>>> futureMapAttr = channel.attr(AttributeKey.valueOf("future"));
        Map<Integer, CompletableFuture<FullHttpResponse>> futureMap = futureMapAttr.get();
        if (null == futureMap) {
            futureMap = new ConcurrentHashMap<>();
            futureMapAttr.set(futureMap);
        }
        return futureMap;
    }

    public static Attribute<String> host(Channel channel) {
        return channel.attr(AttributeKey.valueOf("host"));
    }

    public static Attribute<Integer> port(Channel channel) {
        return channel.attr(AttributeKey.valueOf("port"));
    }

    public static Attribute<Boolean> http2(Channel channel) {
        return channel.attr(AttributeKey.valueOf("http2"));
    }

    public static int genStreamId(Channel channel) {
        Attribute<AtomicInteger> streamIdAttr = channel.attr(AttributeKey.valueOf("streamId"));
        AtomicInteger gen = streamIdAttr.get();
        if (null == gen) {
            gen = new AtomicInteger(101);
            streamIdAttr.set(gen);
        }
        int streamId = gen.addAndGet(2);
        return streamId;
    }
}
