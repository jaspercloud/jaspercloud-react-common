package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Pool {

    private static Logger logger = LoggerFactory.getLogger(Pool.class);

    private List<HttpConnection> list = new ArrayList<>();
    private Map<String, CompletableFuture<HttpConnection>> futureMap = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public Pool(int connections, CreateCall call) {
        for (int i = 0; i < connections; i++) {
            list.add(call.create());
        }
    }

    public AsyncMono<HttpConnection> acquire(String host, int port, long timeout) {
        String uuid = UUID.randomUUID().toString();
        Mono<HttpConnection> mono = Mono.create(sink -> {
            //get same connection
            for (HttpConnection connection : list) {
                if (connection.use(host, port)) {
                    sink.success(connection);
                    return;
                }
            }
            //get connection
            for (HttpConnection connection : list) {
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
            queue.add(uuid);
        });
        if (timeout > 0) {
            mono = mono.timeout(Duration.ofMillis(timeout));
        }
        AsyncMono<HttpConnection> asyncMono = new AsyncMono<>(mono).then(new ReactAsyncCall<HttpConnection, HttpConnection>() {
            @Override
            public void process(boolean hasError, Throwable throwable, HttpConnection result, ReactSink<? super HttpConnection> sink) throws Throwable {
                futureMap.remove(uuid);
                if (hasError) {
                    throw throwable;
                }
                sink.success(result);
            }
        });
        return asyncMono;
    }

    public void release(HttpConnection connection) {
        String uuid;
        while (null != (uuid = queue.poll())) {
            CompletableFuture<HttpConnection> future = futureMap.get(uuid);
            if (null != future) {
                future.complete(connection);
                return;
            }
        }
        connection.release();
    }

    public interface CreateCall {

        HttpConnection create();
    }
}
