package io.jaspercloud.react.http.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.CompletableFuture;

public final class AttributeKeys {

    private AttributeKeys() {
    }

    public static Attribute<CompletableFuture<FullHttpResponse>> future(Channel channel) {
        return channel.attr(AttributeKey.valueOf("future"));
    }
}
