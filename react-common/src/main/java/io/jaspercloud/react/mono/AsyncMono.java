package io.jaspercloud.react.mono;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 链式处理
 * 当前线程 Schedulers.immediate()
 * 可重用的单线程 Schedulers.single()
 * 弹性线程池 Schedulers.elastic() 该方法是一种将阻塞处理放在一个单独的线程中执行的很好的方式
 * 固定大小线程池 Schedulers.parallel() 该方法创建的线程数量取决于CPU的核数。
 *
 * @param <I>
 */
public class AsyncMono<I> {

    private Mono<StreamRecord<I>> input;

    public AsyncMono() {
        this(Mono.empty());
    }

    public AsyncMono(I data) {
        this(Mono.justOrEmpty(data));
    }

    public AsyncMono(Throwable throwable) {
        this(Mono.error(throwable));
    }

    public AsyncMono(Mono<I> head) {
        this.input = Mono.create(new Consumer<MonoSink<StreamRecord<I>>>() {
            @Override
            public void accept(MonoSink<StreamRecord<I>> sink) {
                head.subscribe(new BaseSubscriber<I>() {

                    private I value;
                    private Throwable throwable;

                    @Override
                    protected void hookOnNext(I value) {
                        this.value = value;
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        this.throwable = throwable;
                    }

                    @Override
                    protected void hookFinally(SignalType type) {
                        if (null == throwable) {
                            sink.success(new StreamRecord<>(value));
                        } else {
                            sink.error(throwable);
                        }
                    }
                });
            }
        });
    }

    public AsyncMono(Supplier<Mono<StreamRecord<I>>> supplier) {
        this.input = supplier.get();
    }

    public AsyncMono(AsyncMono<I> input) {
        this.input = input.toMono();
    }

    /**
     * 异步处理
     *
     * @param call
     * @param <O>
     * @return
     */
    public <O> AsyncMono<O> then(ReactAsyncCall<I, O> call) {
        Mono<StreamRecord<O>> result = Mono.create(new Consumer<MonoSink<StreamRecord<O>>>() {
            @Override
            public void accept(MonoSink<StreamRecord<O>> sink) {
                DefaultReactSink reactSink = new DefaultReactSink(sink);
                input.subscribe(new BaseSubscriber<StreamRecord<I>>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        sink.onRequest(value -> subscription.request(value));
                    }

                    @Override
                    protected void hookOnNext(StreamRecord<I> value) {
                        reactSink.setResult(value.getData());
                        try {
                            call.process(false, null, value.getData(), reactSink);
                        } catch (Throwable ex) {
                            reactSink.error(ex);
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        reactSink.setThrowable(throwable);
                        try {
                            call.process(true, throwable, null, reactSink);
                        } catch (Throwable ex) {
                            reactSink.error(ex);
                        }
                    }
                });
            }
        });
        return new AsyncMono<>(new Supplier<Mono<StreamRecord<O>>>() {
            @Override
            public Mono<StreamRecord<O>> get() {
                return result;
            }
        });
    }

    /**
     * 设置超时
     *
     * @param timeout
     * @return
     */
    public AsyncMono<I> timeout(long timeout) {
        return new AsyncMono<>(new Supplier<Mono<StreamRecord<I>>>() {
            @Override
            public Mono<StreamRecord<I>> get() {
                return input.timeout(Duration.ofMillis(timeout));
            }
        });
    }

    /**
     * 影响在其之后的 operator执行的线程池
     *
     * @param scheduler
     * @return
     */
    public AsyncMono<I> publishOn(Scheduler scheduler) {
        return new AsyncMono<>(new Supplier<Mono<StreamRecord<I>>>() {
            @Override
            public Mono<StreamRecord<I>> get() {
                return input.publishOn(scheduler);
            }
        });
    }

    /**
     * 源头影响整个执行过程
     *
     * @param scheduler
     * @return
     */
    public AsyncMono<I> subscribeOn(Scheduler scheduler) {
        return new AsyncMono<>(new Supplier<Mono<StreamRecord<I>>>() {
            @Override
            public Mono<StreamRecord<I>> get() {
                return input.subscribeOn(scheduler);
            }
        });
    }

    public void subscribe(Consumer<? super I> success) {
        input.subscribe(new BaseSubscriber<StreamRecord<I>>() {
            @Override
            protected void hookOnNext(StreamRecord<I> value) {
                success.accept(value.getData());
            }
        });
    }

    public void subscribe(Consumer<? super I> success, Consumer<? super Throwable> error) {
        input.subscribe(new BaseSubscriber<StreamRecord<I>>() {
            @Override
            protected void hookOnNext(StreamRecord<I> value) {
                success.accept(value.getData());
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                error.accept(throwable);
            }
        });
    }

    public void subscribe(Subscriber<? super I> actual) {
        input.subscribe(new Subscriber<StreamRecord<I>>() {
            @Override
            public void onSubscribe(Subscription s) {
                actual.onSubscribe(s);
            }

            @Override
            public void onNext(StreamRecord<I> streamRecord) {
                actual.onNext(streamRecord.getData());
            }

            @Override
            public void onError(Throwable t) {
                actual.onError(t);
            }

            @Override
            public void onComplete() {
                actual.onComplete();
            }
        });
    }

    public void subscribe(ReactSink<? super I> sink) {
        input.subscribe(new BaseSubscriber<StreamRecord<I>>() {
            @Override
            protected void hookOnNext(StreamRecord<I> value) {
                sink.success(value.getData());
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                sink.error(throwable);
            }
        });
    }

    public Mono<StreamRecord<I>> toMono() {
        return input;
    }

    public CompletableFuture<I> toFuture() {
        CompletableFuture<I> future = new CompletableFuture<>();
        input.subscribe(new BaseSubscriber<StreamRecord<I>>() {
            @Override
            protected void hookOnNext(StreamRecord<I> value) {
                future.complete(value.getData());
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    public void subscribe() {
        input.subscribe();
    }

    public static <T> AsyncMono<T> create(Consumer<ReactSink<T>> callback) {
        return new AsyncMono<>(Mono.create(sink -> {
            DefaultReactSink reactSink = new DefaultReactSink(sink);
            try {
                callback.accept(reactSink);
            } catch (Throwable e) {
                reactSink.error(e);
            }
        }));
    }

    public static <T> AsyncMono<T> create(Mono<T> mono) {
        return new AsyncMono<>(mono);
    }
}
