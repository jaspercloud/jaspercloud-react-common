package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ThreadTest {

    @Test
    public void test() throws Exception {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
        threadLocal.set("test");
        ExecutorService executorService = Executors.newCachedThreadPool();
        Mono<String> mono = Mono.create(new Consumer<MonoSink<String>>() {
            @Override
            public void accept(MonoSink<String> monoSink) {
                executorService.execute(() -> {
                    System.out.println(Thread.currentThread());
                    threadLocal.set("accept");
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    monoSink.success("test");
                });
            }
        });
        executorService.execute(() -> {
            new AsyncMono<>(mono).timeout(10 * 1000).then(new ReactAsyncCall<String, String>() {
                @Override
                public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                    System.out.println(Thread.currentThread());
                    String ret = threadLocal.get();
                    System.out.println();
                }
            }).subscribe();
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
