package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class TimeoutTest {

    @Test
    public void test() throws InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        new AsyncMono<String>(Mono.create(sink -> {
            future.thenAccept(e -> sink.success(e));
        })).timeout(3000).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                if (hasError) {
                    throw throwable;
                }
                sink.success(result);
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                System.out.println();
            }

            @Override
            public void onError(Throwable t) {
                System.out.println();
            }

            @Override
            public void onComplete() {

            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
