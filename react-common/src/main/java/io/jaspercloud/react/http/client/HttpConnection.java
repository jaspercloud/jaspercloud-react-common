package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HttpConnection {

    private static Logger logger = LoggerFactory.getLogger(HttpConnection.class);

    private HttpConfig config;
    private NioEventLoopGroup loopGroup;
    private HttpResponseHandler handler;
    private AtomicReference<Channel> reference = new AtomicReference<>();
    private AtomicBoolean status = new AtomicBoolean(false);

    public Channel getChannel() {
        return reference.get();
    }

    boolean http2(String host, int port) {
        Channel channel = reference.get();
        if (null == channel) {
            return false;
        }
        if (!same(channel, host, port)) {
            return false;
        }
        boolean http2 = BooleanUtils.isTrue(AttributeKeys.http2(channel).get());
        return http2;
    }

    boolean use(String host, int port) {
        Channel channel = reference.get();
        if (null == channel) {
            return status.compareAndSet(false, true);
        }
        if (same(channel, host, port)) {
            return status.compareAndSet(false, true);
        }
        return false;
    }

    boolean use() {
        return status.compareAndSet(false, true);
    }

    void release() {
        this.status.set(false);
    }

    private boolean same(Channel channel, String host, int port) {
        String remoteHost = AttributeKeys.host(channel).get();
        int remotePort = AttributeKeys.port(channel).get();
        boolean same = Objects.equals(host, remoteHost) && Objects.equals(port, remotePort);
        return same;
    }

    public HttpConnection(HttpConfig config, NioEventLoopGroup loopGroup, HttpResponseHandler handler) {
        this.config = config;
        this.loopGroup = loopGroup;
        this.handler = handler;
    }

    public AsyncMono<Channel> connect(String host, int port, boolean ssl) {
        return new AsyncMono(Mono.create(new Consumer<MonoSink<Channel>>() {
            @Override
            public void accept(MonoSink<Channel> sink) {
                doConnect(host, port, ssl, sink);
            }
        }));
    }

    private void doConnect(String host, int port, boolean ssl, MonoSink<Channel> sink) {
        Channel channel = reference.get();
        if (null != channel) {
            if (!same(channel, host, port)) {
                channel.close();
                reference.set(null);
            }
        }
        channel = reference.get();
        if (null != channel && channel.isActive()) {
            sink.success(channel);
            return;
        }
        ChannelFuture future = new Bootstrap()
                .group(loopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (ssl) {
                            SslHandler sslHandler = config.getSslContext().newHandler(ch.alloc());
                            sslHandler.setHandshakeTimeoutMillis(config.getHandshakeTimeoutMillis());
                            sslHandler.setCloseNotifyFlushTimeoutMillis(config.getCloseNotifyFlushTimeoutMillis());
                            sslHandler.setCloseNotifyReadTimeoutMillis(config.getCloseNotifyReadTimeoutMillis());
                            pipeline.addLast(sslHandler);
                        }
                        HttpClientCodec httpCodec = new HttpClientCodec(
                                config.getMaxInitialLineLength(),
                                config.getMaxHeaderSize(),
                                config.getMaxChunkSize(),
                                config.isFailOnMissingResponse(),
                                config.isValidateHeaders());
                        pipeline.addLast("httpCodec", httpCodec);
                        pipeline.addLast(new HttpContentDecompressor());
                        //http2
                        supportHttp2(pipeline, httpCodec);
                        pipeline.addLast(handler);
                    }
                }).connect(new InetSocketAddress(host, port));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    AttributeKeys.host(channel).set(host);
                    AttributeKeys.port(channel).set(port);
                    reference.set(channel);
                    sink.success(channel);
                } else {
                    sink.error(future.cause());
                }
            }
        });
    }

    private void supportHttp2(ChannelPipeline pipeline, HttpClientCodec httpCodec) {
        Http2Connection connection = new DefaultHttp2Connection(false);
        HttpToHttp2ConnectionHandler connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
                .frameListener(new InboundHttp2ToHttpAdapterBuilder(connection)
                        .maxContentLength(config.getMaxContentLength())
                        .propagateSettings(true)
                        .build())
                .connection(connection)
                .build();
        Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler);
        HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(httpCodec, upgradeCodec, config.getMaxContentLength());
        pipeline.addLast(upgradeHandler);
        pipeline.addLast(new ChannelDuplexHandler() {
            @Override
            public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
                ChannelPromise connectPromise = ctx.newPromise();
                super.connect(ctx, remoteAddress, localAddress, connectPromise);
                connectPromise.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        //send upgradeRequest
                        FullHttpRequest upgradeRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/", Unpooled.EMPTY_BUFFER);
                        ctx.writeAndFlush(upgradeRequest);
                        SimpleChannelInboundHandler<HttpResponse> http1Handler = new SimpleChannelInboundHandler<HttpResponse>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, HttpResponse msg) throws Exception {
                                if (!promise.isDone()) {
                                    ctx.pipeline().addAfter("httpCodec", "httpAgg", new HttpObjectAggregator(config.getMaxContentLength()));
                                    ctx.pipeline().remove(this);
                                    AttributeKeys.http2(ctx.channel()).set(false);
                                    promise.setSuccess();
                                }
                            }
                        };
                        ctx.pipeline().addLast(http1Handler, new SimpleChannelInboundHandler<Http2Settings>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Http2Settings msg) throws Exception {
                                ctx.pipeline().remove(http1Handler);
                                AttributeKeys.http2(ctx.channel()).set(true);
                                promise.setSuccess();
                            }
                        });
                    }
                });
            }
        });
    }
}
