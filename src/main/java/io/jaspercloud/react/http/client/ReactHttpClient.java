package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.Attribute;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ReactHttpClient
 * 用于异步高并发请求处理
 */
public class ReactHttpClient {

    private static Logger logger = LoggerFactory.getLogger(ReactHttpClient.class);

    private HttpConfig config;
    private NioEventLoopGroup loopGroup;
    private HttpConnectionPool httpPool;

    public ReactHttpClient() {
        this(new HttpConfig());
    }

    public ReactHttpClient(HttpConfig config) {
        this.config = config;
        //thread
        loopGroup = new NioEventLoopGroup(config.getLoopThread(), new ThreadFactory() {

            private AtomicLong counter = new AtomicLong();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, config.getThreadName() + counter.incrementAndGet());
            }
        });
        //httpPool
        httpPool = new SimplePool(config.getPoolSize(), new HttpConnectionPool.HttpConnectionCteate() {
            @Override
            public HttpConnection create() {
                return new HttpConnection(config, loopGroup, new HttpResponseHandler() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
                        Attribute<CompletableFuture<FullHttpResponse>> attribute = AttributeKeys.future(ctx.channel());
                        CompletableFuture<FullHttpResponse> future = attribute.getAndSet(null);
                        if (null != future) {
                            future.complete(msg);
                        }
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        Attribute<CompletableFuture<FullHttpResponse>> attribute = AttributeKeys.future(ctx.channel());
                        CompletableFuture<FullHttpResponse> future = attribute.getAndSet(null);
                        if (null != future) {
                            future.completeExceptionally(cause);
                        }
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        Attribute<CompletableFuture<FullHttpResponse>> attribute = AttributeKeys.future(ctx.channel());
                        CompletableFuture<FullHttpResponse> future = attribute.getAndSet(null);
                        if (null != future) {
                            future.completeExceptionally(new ChannelException("channel closed"));
                        }
                    }
                });
            }
        });
    }

    public AsyncMono<Response> execute(Request request) {
        return doExecute(request, config.getReadTimeout());
    }

    public AsyncMono<Response> execute(Request request, long timeout) {
        return doExecute(request, timeout);
    }

    /**
     * 请求
     *
     * @param request
     * @return
     */
    private AsyncMono<Response> doExecute(Request request, long timeout) {
        AsyncMono<Response> asyncMono = httpPool.acquire(request.url().host(), request.url().port(), config.getConnectionTimeout())
                .then(new ReactAsyncCall<HttpConnection, Response>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, HttpConnection connection, ReactSink<? super Response> sink) throws Throwable {
                        if (hasError) {
                            sink.error(throwable);
                            return;
                        }
                        //connect and request
                        connection.connect(request.url().host(), request.url().port(), request.isHttps())
                                .then(new RequestProcessor(config, request))
                                //wait response timeout
                                .timeout(timeout)
                                .then(new ResponseProcessor(request))
                                .then(new ReactAsyncCall<Response, Response>() {
                                    @Override
                                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Response> sink) throws Throwable {
                                        httpPool.release(connection);
                                        sink.finish();
                                    }
                                })
                                .subscribe(sink);
                    }
                });
        return asyncMono;
    }
}
