package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimplePool implements HttpConnectionPool {

    private static Logger logger = LoggerFactory.getLogger(SimplePool.class);

    private List<HttpConnection> list = new ArrayList<>();
    private Map<String, CompletableFuture<HttpConnection>> futureMap = new ConcurrentHashMap<>();
    private Map<String, BlockingQueue<CompletableFuture<HttpConnection>>> futureListMap = new ConcurrentHashMap<>();
    private BlockingQueue<String> waitFutureIdQueue = new LinkedBlockingQueue<>();
    private Lock lock = new ReentrantLock();

    public SimplePool(int connections, HttpConnectionCreate call) {
        for (int i = 0; i < connections; i++) {
            list.add(call.create());
        }
    }

    @Override
    public AsyncMono<HttpConnection> acquire(String host, int port, long timeout) {
        String uuid = UUID.randomUUID().toString();
        Mono<HttpConnection> mono = Mono.create(sink -> {
            try {
                for (HttpConnection connection : list) {
                    if (connection.http2(host, port)) {
                        sink.success(connection);
                        return;
                    }
                    if (connection.use(host, port)) {
                        sink.success(connection);
                        return;
                    }
                    if (connection.use()) {
                        sink.success(connection);
                        return;
                    }
                }
                //not found
                CompletableFuture<HttpConnection> future = new CompletableFuture();
                future.thenAccept(connection -> {
                    sink.success(connection);
                });
                //add wait
                futureMap.put(uuid, future);
                String key = String.format("%s:%s", host, port);
                addQueue(key, future);
                waitFutureIdQueue.add(uuid);
            } catch (Throwable e) {
                sink.error(e);
            }
        });
        if (timeout > 0) {
            mono = mono.timeout(Duration.ofMillis(timeout));
        }
        AsyncMono<HttpConnection> asyncMono = new AsyncMono<>(mono).then(new ReactAsyncCall<HttpConnection, HttpConnection>() {
            @Override
            public void process(boolean hasError, Throwable throwable, HttpConnection result, ReactSink<? super HttpConnection> sink) throws Throwable {
                futureMap.remove(uuid);
                sink.finish();
            }
        });
        return asyncMono;
    }

    private void addQueue(String key, CompletableFuture<HttpConnection> future) {
        lock.lock();
        try {
            BlockingQueue<CompletableFuture<HttpConnection>> queue = futureListMap.computeIfAbsent(key, s -> new LinkedBlockingQueue<>());
            queue.add(future);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void release(HttpConnection connection) {
        Channel channel = connection.getChannel();
        if (null == channel) {
            connection.release();
            return;
        }
        //get same connection
        String host = AttributeKeys.host(channel).get();
        int port = AttributeKeys.port(connection.getChannel()).get();
        String key = String.format("%s:%s", host, port);
        BlockingQueue<CompletableFuture<HttpConnection>> queue = futureListMap.get(key);
        if (null != queue) {
            CompletableFuture<HttpConnection> future = queue.poll();
            if (null != future) {
                future.complete(connection);
                lock.lock();
                try {
                    if (queue.isEmpty()) {
                        futureListMap.remove(key);
                    }
                } finally {
                    lock.unlock();
                }
                return;
            }
        }
        //get connection
        String uuid;
        while (null != (uuid = waitFutureIdQueue.poll())) {
            CompletableFuture<HttpConnection> future = futureMap.get(uuid);
            if (null != future) {
                future.complete(connection);
                return;
            }
        }
        //not found
        connection.release();
    }
}
