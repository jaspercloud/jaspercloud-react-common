package io.jaspercloud.react.mono.logical;

import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.jaspercloud.react.mono.StreamRecord;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import okhttp3.Request;
import okhttp3.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Logical<I, O> {

    private List<AsyncMono<I>> monoList;

    public Logical(List<AsyncMono<I>> monoList) {
        this.monoList = monoList;
    }

    public static <O> void main(String[] args) throws Exception {
        Channel channel = new AsyncMono<>(Mono.create(new Consumer<MonoSink<Channel>>() {
            @Override
            public void accept(MonoSink<Channel> sink) {
                sink.success(new EmbeddedChannel());
            }
        })).toFuture().get();

        ReactHttpClient reactHttpClient = new ReactHttpClient();
        List<String> list = Arrays.asList(
                "https://www.baidu.com",
                "https://www.youku.com",
                "https://www.hao123.com"
        );
        List<AsyncMono<Response>> collect = list.stream().map(e -> {
            AsyncMono<Response> asyncMono = reactHttpClient.execute(new Request.Builder().url(e).build())
                    .timeout(30 * 1000);
            return asyncMono;
        }).collect(Collectors.toList());
        List<Integer> codeList = new Logical<Response, Response>(collect).collect().then(new ReactAsyncCall<List<Response>, List<Integer>>() {
            @Override
            public void process(boolean hasError, Throwable throwable, List<Response> result, ReactSink<? super List<Integer>> sink) throws Throwable {
                if (hasError) {
                    sink.finish();
                    return;
                }
                sink.success(result.stream().map(e -> e.code()).collect(Collectors.toList()));
            }
        }).toFuture().get();


        new AsyncMono<>("test")
                .then(new ReactAsyncCall<String, String>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                        sink.success(result);
                    }
                })
                .subscribe(new ReactSink<String>() {
                    @Override
                    public void success() {
                        System.out.println();
                    }

                    @Override
                    public void success(String s) {
                        System.out.println();
                    }

                    @Override
                    public void error(Throwable throwable) {
                        System.out.println();
                    }

                    @Override
                    public void finish() {
                        System.out.println();
                    }
                });

        ArrayList<String> tmp = new ArrayList<>(list);
        tmp.add(null);
        String s = new Logical<String, String>(tmp.stream().map(e -> new AsyncMono<>(e)).collect(Collectors.toList()))
                .collect().then(new ReactAsyncCall<List<String>, String>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, List<String> result, ReactSink<? super String> sink) throws Throwable {
                        System.out.println();
                    }
                }).toFuture().get();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
        System.out.println();
    }

    public AsyncMono<O> selectOne(SelectOneCall<I> call) {
        Iterator<AsyncMono<I>> iterator = monoList.iterator();
        return AsyncMono.create(Mono.create(new Consumer<MonoSink<O>>() {
            @Override
            public void accept(MonoSink<O> monoSink) {
                new Operation(iterator, call, monoSink).doNext();
            }
        }));
    }

    public AsyncMono<List<O>> collect() {
        List<Mono<StreamRecord<I>>> collect = monoList.stream().map(e -> e.toMono()).collect(Collectors.toList());
        Mono<List<StreamRecord<I>>> mono = Flux.concat(collect).collectList();
        AsyncMono<List<O>> asyncMono = AsyncMono.create(mono).then(new ReactAsyncCall<List<StreamRecord<I>>, List<O>>() {
            @Override
            public void process(boolean hasError, Throwable throwable, List<StreamRecord<I>> result, ReactSink<? super List<O>> sink) throws Throwable {
                if (hasError) {
                    sink.finish();
                    return;
                }
                List<O> list = result.stream().map(e -> (O) e.getData()).collect(Collectors.toList());
                sink.success(list);
            }
        });
        return asyncMono;
    }

    public interface SelectOneCall<I> {

        void onCall(boolean hasError, Throwable throwable, I result, Operation operation);
    }

    public static class Operation<I, O> {

        private Iterator<AsyncMono<I>> iterator;
        private SelectOneCall<I> call;
        private MonoSink<O> monoSink;
        private AtomicBoolean status = new AtomicBoolean();

        public Operation(Iterator<AsyncMono<I>> iterator, SelectOneCall<I> call, MonoSink<O> monoSink) {
            this.iterator = iterator;
            this.call = call;
            this.monoSink = monoSink;
        }

        public void success(I result) {
            if (status.compareAndSet(false, true)) {
                monoSink.success((O) result);
            }
        }

        public void error(Throwable throwable) {
            if (status.compareAndSet(false, true)) {
                monoSink.error(throwable);
            }
        }

        public void doNext() {
            if (status.compareAndSet(false, true)) {
                if (!iterator.hasNext()) {
                    monoSink.success();
                    return;
                }
                AsyncMono<I> next = iterator.next();
                next.then(new ReactAsyncCall<I, O>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, I result, ReactSink<? super O> sink) throws Throwable {
                        call.onCall(hasError, throwable, result, new Operation(iterator, call, monoSink));
                    }
                }).subscribe();
            }
        }
    }
}
