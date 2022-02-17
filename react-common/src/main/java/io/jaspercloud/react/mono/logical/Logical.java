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

public class Logical<I> {

    private List<AsyncMono<I>> monoList;

    public Logical(List<AsyncMono<I>> monoList) {
        this.monoList = monoList;
    }

    public static <I> Logical<I> create(List<AsyncMono<I>> monoList) {
        return new Logical<>(monoList);
    }

    public <O> AsyncMono<O> selectOne(SelectOneCall<I, O> call) {
        Iterator<AsyncMono<I>> iterator = monoList.iterator();
        return AsyncMono.create(new Consumer<ReactSink<O>>() {
            @Override
            public void accept(ReactSink<O> monoSink) {
                new Operation<>(iterator, call, monoSink).doNext();
            }
        });
    }

    public AsyncMono<List<I>> collect() {
        List<Mono<StreamRecord<I>>> collect = monoList.stream().map(e -> e.toMonoRecord()).collect(Collectors.toList());
        Mono<List<StreamRecord<I>>> mono = Flux.concat(collect).collectList();
        AsyncMono<List<I>> asyncMono = new AsyncMono<>(mono).then(new ReactAsyncCall<List<StreamRecord<I>>, List<I>>() {
            @Override
            public void process(boolean hasError, Throwable throwable, List<StreamRecord<I>> result, ReactSink<? super List<I>> sink) throws Throwable {
                if (hasError) {
                    sink.finish();
                    return;
                }
                List<I> list = result.stream().map(e -> e.getData()).collect(Collectors.toList());
                sink.success(list);
            }
        });
        return asyncMono;
    }

    public interface SelectOneCall<I, O> {

        void onCall(boolean hasError, Throwable throwable, I result, Operation<I, O> operation);

        default O onFinally() {
            return null;
        }
    }

    public static class Operation<I, O> {

        private Iterator<AsyncMono<I>> iterator;
        private SelectOneCall<I, O> call;
        private ReactSink<O> monoSink;
        private AtomicBoolean status = new AtomicBoolean();

        public Operation(Iterator<AsyncMono<I>> iterator, SelectOneCall<I, O> call, ReactSink<O> monoSink) {
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
                    monoSink.success(call.onFinally());
                    return;
                }
                AsyncMono<I> next = iterator.next();
                next.then(new ReactAsyncCall<I, O>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, I result, ReactSink<? super O> sink) throws Throwable {
                        call.onCall(hasError, throwable, result, new Operation<>(iterator, call, monoSink));
                    }
                }).subscribe();
            }
        }
    }
}
