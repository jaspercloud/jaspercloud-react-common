package io.jaspercloud.react.mono;

import io.jaspercloud.react.mono.invoke.InvokeRequest;
import io.jaspercloud.react.mono.invoke.InvokeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ReactInvokeChain {

    private ReactInvokeChain() {

    }

    public static <K, T, O> AsyncMono<O> filterCall(List<InvokeRequest<K, T>> monoList, FilterCallback<K, T, O> call) {
        AsyncMono<O> asyncMono = new AsyncMono<>(Mono.create(new Consumer<MonoSink<O>>() {
            @Override
            public void accept(MonoSink<O> sink) {
                try {
                    Iterator<InvokeRequest<K, T>> iterator = monoList.iterator();
                    if (!iterator.hasNext()) {
                        sink.success(call.notFound());
                        return;
                    }
                    doNext(iterator, iterator.next(), sink, call);
                } catch (Throwable e) {
                    sink.error(e);
                }
            }
        }));
        return asyncMono;
    }

    public static <K, T, O> AsyncMono<O> mergeCall(List<InvokeRequest<K, T>> monoList, MergeCallback<K, T, O> callback) {
        Mono<O> mono = Mono.create(new Consumer<MonoSink<O>>() {
            @Override
            public void accept(MonoSink<O> sink) {
                List<InvokeResponse<K, T>> resultList = new CopyOnWriteArrayList<>();
                if (monoList.isEmpty()) {
                    sink.success(callback.process(resultList));
                    return;
                }
                monoList.forEach(e -> {
                    K key = e.getKey();
                    e.getRoute().subscribeOn(Schedulers.parallel()).then(new ReactAsyncCall<T, Void>() {
                        @Override
                        public void process(boolean hasError, Throwable throwable, T result, ReactSink<? super Void> _) throws Throwable {
                            try {
                                if (hasError) {
                                    resultList.add(new InvokeResponse<>(key, throwable));
                                } else {
                                    resultList.add(new InvokeResponse<>(key, result));
                                }
                            } finally {
                                if (resultList.size() == monoList.size()) {
                                    sink.success(callback.process(resultList));
                                }
                            }
                        }
                    }).subscribe();
                });
            }
        });
        return new AsyncMono<>(mono);
    }

    private static <T, K, O> void doNext(Iterator<InvokeRequest<K, T>> iterator,
                                         InvokeRequest<K, T> mono, MonoSink<O> sink,
                                         FilterCallback<K, T, O> call) {
        K key = mono.getKey();
        AsyncMono<T> asyncMono = mono.getRoute();
        asyncMono.subscribeOn(Schedulers.parallel()).then(new ReactAsyncCall<T, O>() {
            @Override
            public void process(boolean hasError, Throwable throwable, T result, ReactSink<? super O> _) throws Throwable {
                try {
                    ResponseEntity<O> entity = call.process(key, hasError, throwable, result);
                    if (HttpStatus.OK.value() != entity.getStatusCodeValue()) {
                        if (iterator.hasNext()) {
                            doNext(iterator, iterator.next(), sink, call);
                        } else {
                            sink.success(call.notFound());
                        }
                    } else {
                        sink.success(entity.getBody());
                    }
                } catch (Throwable ex) {
                    sink.error(ex);
                }
            }
        }).subscribe();
    }

    public interface FilterCallback<K, I, O> {

        ResponseEntity<O> process(K key, boolean hasError, Throwable throwable, I result);

        default O notFound() {
            return null;
        }
    }

    public interface MergeCallback<K, T, O> {

        O process(List<InvokeResponse<K, T>> list);
    }
}
