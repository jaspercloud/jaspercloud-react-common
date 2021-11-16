package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HttpConnection {

    private static Logger logger = LoggerFactory.getLogger(HttpConnection.class);

    private static final String Host = "host";
    private static final String Port = "port";

    private HttpConfig config;
    private NioEventLoopGroup loopGroup;
    private HttpResponseHandler handler;
    private AtomicReference<Channel> reference = new AtomicReference<>();
    private AtomicBoolean status = new AtomicBoolean(false);

    public boolean use(String host, int port) {
        Channel channel = reference.get();
        if (null == channel) {
            return status.compareAndSet(false, true);
        }
        String remoteHost = (String) channel.attr(AttributeKey.valueOf(Host)).get();
        int remotePort = (int) channel.attr(AttributeKey.valueOf(Port)).get();
        if ((Objects.equals(host, remoteHost) && Objects.equals(port, remotePort))) {
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
            String remoteHost = (String) channel.attr(AttributeKey.valueOf(Host)).get();
            int remotePort = (int) channel.attr(AttributeKey.valueOf(Port)).get();
            if (!(Objects.equals(host, remoteHost) && Objects.equals(port, remotePort))) {
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
                .option(ChannelOption.SO_REUSEADDR, true)
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
                        pipeline.addLast(new HttpClientCodec(config.getMaxInitialLineLength(),
                                config.getMaxHeaderSize(),
                                config.getMaxChunkSize(),
                                config.isFailOnMissingResponse(),
                                config.isValidateHeaders()));
                        pipeline.addLast(new HttpObjectAggregator(config.getMaxContentLength()));
                        pipeline.addLast(handler);
                    }
                }).connect(new InetSocketAddress(host, port));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    channel.attr(AttributeKey.valueOf(Host)).set(host);
                    channel.attr(AttributeKey.valueOf(Port)).set(port);
                    reference.set(channel);
                    sink.success(channel);
                } else {
                    sink.error(future.cause());
                }
            }
        });
    }
}
