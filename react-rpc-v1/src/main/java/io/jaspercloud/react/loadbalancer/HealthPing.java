package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public final class HealthPing {

    private static final NioEventLoopGroup loopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

    private HealthPing() {

    }

    public static AsyncMono<Boolean> check(String host, int port, int timeout) {
        AsyncMono<Boolean> asyncMono = AsyncMono.<Boolean>create(sink -> {
            ChannelFuture future = new Bootstrap()
                    .group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                        }
                    }).connect(new InetSocketAddress(host, port));
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Channel channel = future.channel();
                    try {
                        sink.success(future.isSuccess() && channel.isActive());
                    } finally {
                        channel.close();
                    }
                }
            });
        });
        return asyncMono;
    }
}
