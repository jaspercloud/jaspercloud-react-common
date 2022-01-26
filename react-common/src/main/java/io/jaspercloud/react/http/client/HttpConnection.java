package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactSink;
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
import io.netty.channel.SimpleUserEventChannelHandler;
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
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HttpConnection {

    private static Logger logger = LoggerFactory.getLogger(HttpConnection.class);

    public static final String Http1Codec = "Http1Codec";
    public static final String Bridge = "Bridge";
    public static final String UpgradeHandler = "UpgradeHandler";
    public static final String ConnectHandler = "ConnectHandler";
    public static final String Http2SettingsHandler = "Http2SettingsHandler";

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

    public AsyncMono<Channel> connect(String host, int port, boolean ssl, boolean tryHttp2) {
        return AsyncMono.create(new Consumer<ReactSink<Channel>>() {
            @Override
            public void accept(ReactSink<Channel> sink) {
                doConnect(host, port, ssl, tryHttp2, sink);
            }
        });
    }

    private void doConnect(String host, int port, boolean ssl, boolean tryHttp2, ReactSink<Channel> sink) {
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
        Bootstrap bootstrap = new Bootstrap()
                .group(loopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        ChannelFutureListener connectFutureListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    AttributeKeys.host(channel).set(host);
                    AttributeKeys.port(channel).set(port);
                    reference.set(channel);
                    sink.success(channel);
                } else if (future.cause() instanceof UnsupportedHttp2Exception) {
                    future.channel().close();
                    //use http1
                    createConnectHandler(bootstrap, ssl, false)
                            .connect(new InetSocketAddress(host, port))
                            .addListener(this);
                } else {
                    future.channel().close();
                    sink.error(future.cause());
                }
            }
        };
        createConnectHandler(bootstrap, ssl, tryHttp2)
                .connect(new InetSocketAddress(host, port))
                .addListener(connectFutureListener);
    }

    private Bootstrap createConnectHandler(Bootstrap bootstrap, boolean ssl, boolean http2) {
        return bootstrap.handler(new ChannelInitializer<SocketChannel>() {
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
                pipeline.addLast(Http1Codec, httpCodec);
                pipeline.addLast(Bridge, new ChannelDuplexHandler());
                //http2
                if (http2) {
                    supportHttp2(pipeline, httpCodec);
                }
                pipeline.addLast(new HttpContentDecompressor());
                pipeline.addLast(new HttpObjectAggregator(config.getMaxContentLength()));
                pipeline.addLast(handler);
            }
        });
    }

    private void supportHttp2(ChannelPipeline pipeline, HttpClientCodec httpCodec) {
        HttpClientUpgradeHandler upgradeHandler = createHttpClientUpgradeHandler(httpCodec);
        pipeline.addAfter(Bridge, UpgradeHandler, upgradeHandler);
        ChannelDuplexHandler connectHttp2Handler = new ChannelDuplexHandler() {
            @Override
            public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise connectPromise) throws Exception {
                ChannelPromise http2Promise = ctx.newPromise();
                super.connect(ctx, remoteAddress, localAddress, http2Promise);
                http2Promise.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        pipeline.addAfter(ConnectHandler, Http2SettingsHandler, new SimpleUserEventChannelHandler<HttpClientUpgradeHandler.UpgradeEvent>() {
                            @Override
                            protected void eventReceived(ChannelHandlerContext ctx, HttpClientUpgradeHandler.UpgradeEvent evt) throws Exception {
                                if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_SUCCESSFUL.equals(evt)) {
                                    AttributeKeys.http2(ctx.channel()).set(true);
                                    connectPromise.setSuccess();
                                } else if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_REJECTED.equals(evt)) {
                                    AttributeKeys.http2(ctx.channel()).set(false);
                                    connectPromise.setFailure(new UnsupportedHttp2Exception());
                                } else if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_ISSUED.equals(evt)) {

                                } else {
                                    throw new UnsupportedOperationException();
                                }
                            }
                        });
                        //send upgradeRequest
                        FullHttpRequest upgradeRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/", Unpooled.EMPTY_BUFFER);
                        ctx.writeAndFlush(upgradeRequest);
                    }
                });
            }
        };
        pipeline.addAfter(UpgradeHandler, ConnectHandler, connectHttp2Handler);
    }

    private HttpClientUpgradeHandler createHttpClientUpgradeHandler(HttpClientCodec httpCodec) {
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
        return upgradeHandler;
    }
}
