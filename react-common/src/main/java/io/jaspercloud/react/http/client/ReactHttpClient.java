package io.jaspercloud.react.http.client;

import io.jaspercloud.react.exception.ReactException;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.netty.channel.nio.NioEventLoopGroup;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
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
    private Semaphore semaphore;

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
        HttpResponseHandler responseHandler = new HttpResponseHandler();
        httpPool = new SimplePool(config.getPoolSize(), new HttpConnectionPool.HttpConnectionCreate() {
            @Override
            public HttpConnection create() {
                return new HttpConnection(config, loopGroup, responseHandler);
            }
        });
        if (config.getMaxConcurrent() > 0) {
            this.semaphore = new Semaphore(config.getMaxConcurrent());
        }
        //checkExecutionHandler
        if (null == config.getExecutionHandler()) {
            throw new ReactException("not found executionHandler");
        }
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
        //checkSemaphore
        if (null != semaphore && !semaphore.tryAcquire()) {
            return config.getExecutionHandler().rejectedExecution(request, this);
        }
        AsyncMono<Response> asyncMono = httpPool.acquire(request.url().host(), request.url().port(), config.getConnectionTimeout())
                .then(new ReactAsyncCall<HttpConnection, Response>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, HttpConnection connection, ReactSink<? super Response> sink) throws Throwable {
                        if (hasError) {
                            sink.error(throwable);
                            return;
                        }
                        //connect and request
                        String http2Header = request.headers().get("http2");
                        boolean tryHttp2 = null == http2Header ? false : Boolean.parseBoolean(http2Header);
                        connection.connect(request.url().host(), request.url().port(), request.isHttps(), tryHttp2)
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
                }).then(new ReactAsyncCall<Response, Response>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Response> sink) throws Throwable {
                        if (null != semaphore) {
                            semaphore.release();
                        }
                        if (hasError) {
                            sink.error(throwable);
                            return;
                        }
                        sink.success(result);
                    }
                });
        return asyncMono;
    }
}
