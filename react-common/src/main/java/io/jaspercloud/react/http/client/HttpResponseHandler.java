package io.jaspercloud.react.http.client;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ChannelHandler.Sharable
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        Map<Integer, CompletableFuture<FullHttpResponse>> futureMap = AttributeKeys.future(ctx.channel());
        CompletableFuture<FullHttpResponse> future;
        if (BooleanUtils.isTrue(AttributeKeys.http2(ctx.channel()).get())) {
            Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
            future = futureMap.remove(streamId);
        } else {
            future = futureMap.remove(AttributeKeys.DefaultStreamId);
        }
        if (null != future) {
            future.complete(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Map<Integer, CompletableFuture<FullHttpResponse>> futureMap = AttributeKeys.future(ctx.channel());
        Iterator<Integer> iterator = futureMap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            CompletableFuture<FullHttpResponse> future = futureMap.remove(key);
            if (null != future) {
                future.completeExceptionally(cause);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Map<Integer, CompletableFuture<FullHttpResponse>> futureMap = AttributeKeys.future(ctx.channel());
        Iterator<Integer> iterator = futureMap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            CompletableFuture<FullHttpResponse> future = futureMap.remove(key);
            if (null != future) {
                future.completeExceptionally(new ChannelException("channel closed"));
            }
        }
    }
}
