package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.netty.handler.codec.http.HttpMethod;
import okhttp3.Request;
import okhttp3.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ReactHttpClient
 * 用于异步高并发请求处理
 */
public class ReactHttpClient {

    private Scheduler workScheduler;

    /**
     * @param threads 逻辑处理线程数
     */
    public ReactHttpClient(int threads) {
        this(Schedulers.fromExecutor(Executors.newFixedThreadPool(threads, new DefaultThreadFactory())));
    }

    /**
     * @param workScheduler 自定义调度器
     */
    public ReactHttpClient(Scheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    /**
     * 请求
     *
     * @param request
     * @return
     */
    public AsyncMono<Response> execute(Request request) {
        Mono<Response> mono = HttpClient.create()
                .keepAlive(true)
                .headers(new ProcessHeader(request))
                .request(HttpMethod.valueOf(request.method()))
                .uri(request.url().toString())
                .send((req, out) -> {
                    return out.sendByteArray(Flux.create(new ProcessRequestBody(request)));
                })
                .responseSingle(new ProcessResponse(request))
                .publishOn(workScheduler);
        return new AsyncMono<>(mono);
    }

    /**
     * 请求
     *
     * @param request
     * @param timeout 超时时间
     * @return
     */
    public AsyncMono<Response> execute(Request request, long timeout) {
        return execute(request).timeout(timeout);
    }

    public static class DefaultThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public DefaultThreadFactory() {
            group = new ThreadGroup("reactWorker");
            namePrefix = "reactPool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
