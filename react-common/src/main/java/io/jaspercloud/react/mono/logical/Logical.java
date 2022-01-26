package io.jaspercloud.react.mono.logical;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.jaspercloud.react.mono.StreamRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Logical<I, O> {

    private List<AsyncMono<I>> monoList;

    public Logical(List<AsyncMono<I>> monoList) {
        this.monoList = monoList;
    }

    public AsyncMono<O> selectOne(SelectOneCall<I> call) {
        Iterator<AsyncMono<I>> iterator = monoList.iterator();
        return AsyncMono.create(new Consumer<ReactSink<O>>() {
            @Override
            public void accept(ReactSink<O> monoSink) {
                new Operation(iterator, call, monoSink).doNext();
            }
        });
    }

    public AsyncMono<List<O>> collect() {
        List<Mono<StreamRecord<I>>> collect = monoList.stream().map(e -> e.toMono()).collect(Collectors.toList());
        Mono<List<StreamRecord<I>>> mono = Flux.concat(collect).collectList();
        AsyncMono<List<O>> asyncMono = new AsyncMono<>(mono).then(new ReactAsyncCall<List<StreamRecord<I>>, List<O>>() {
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
        private ReactSink<O> monoSink;
        private AtomicBoolean status = new AtomicBoolean();

        public Operation(Iterator<AsyncMono<I>> iterator, SelectOneCall<I> call, ReactSink<O> monoSink) {
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
