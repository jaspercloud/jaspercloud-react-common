package io.jaspercloud.react.http.client.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import okhttp3.Request;
import okhttp3.internal.Util;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NettyClient {

    private static SslContext DefaultSslContext;

    static {
        try {
            DefaultSslContext = SslContextBuilder.forClient()
                    .sslProvider(SslProvider.JDK)
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public NettyClient() {

    }

    public FullHttpResponse execute(Request request) throws Exception {
        CompletableFuture<FullHttpResponse> completableFuture = new CompletableFuture<>();
        ChannelFuture future = new Bootstrap()
                .group(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (request.url().isHttps()) {
                            SslHandler sslHandler = DefaultSslContext.newHandler(ch.alloc());
                            pipeline.addLast(sslHandler);
                        }
                        pipeline.addLast(new HttpClientCodec(4096, 8192, 8192, false));
                        pipeline.addLast(new HttpObjectAggregator(4 * 1024 * 1024));
                        pipeline.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
                                completableFuture.complete(msg);
                            }
                        });
                    }
                }).connect(new InetSocketAddress(request.url().host(), request.url().port()));
        Channel channel = future.sync().channel();
        HttpVersion httpVersion = HttpVersion.HTTP_1_1;
        HttpMethod method = HttpMethod.valueOf(request.method());
        String uri = request.url().toString();
        CompositeByteBuf content = channel.alloc().compositeBuffer();
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
        channel.writeAndFlush(fullHttpRequest);
        FullHttpResponse response = completableFuture.get();
        return response;
    }
}
