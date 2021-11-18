package io.jaspercloud.react.mono;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.function.Consumer;

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

    private Mono<I> input;

    public Mono<I> toMono() {
        return input;
    }

    public AsyncMono() {
        this(Mono.empty());
    }

    public AsyncMono(I data) {
        this(Mono.justOrEmpty(data));
    }

    public AsyncMono(Throwable throwable) {
        this(Mono.error(throwable));
    }

    public AsyncMono(Mono<I> input) {
        this.input = input;
    }

    public AsyncMono(AsyncMono<I> input) {
        this.input = input.toMono();
    }

    public static <T> AsyncMono<T> create(Consumer<MonoSink<T>> callback) {
        return new AsyncMono<>(Mono.create(callback));
    }

    /**
     * 异步处理
     *
     * @param call
     * @param <O>
     * @return
     */
    public <O> AsyncMono<O> then(ReactAsyncCall<I, O> call) {
        Mono<O> result = Mono.create(new Consumer<MonoSink<O>>() {
            @Override
            public void accept(MonoSink<O> sink) {
                ReactSink<O> reactSink = new ReactSink<O>() {

                    @Override
                    public void success(O o) {
                        sink.success(o);
                    }

                    @Override
                    public void error(Throwable throwable) {
                        sink.error(throwable);
                    }
                };
                /**
                 * BaseSubscriber会触发hookFinally
                 * CoreSubscriber、Subscriber不会触发
                 */
                input.subscribe(new BaseSubscriber<I>() {

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        sink.onRequest(e -> {
                            call.onSubscribe();
                            subscription.request(e);
                        });
                    }

                    @Override
                    protected void hookOnNext(I next) {
                        try {
                            call.process(false, null, next, reactSink);
                        } catch (Throwable t) {
                            reactSink.error(t);
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        try {
                            call.process(true, throwable, null, reactSink);
                        } catch (Throwable t) {
                            reactSink.error(t);
                        }
                    }

                    @Override
                    protected void hookFinally(SignalType type) {
                        call.onFinally();
                    }
                });
            }
        });
        return new AsyncMono<>(result);
    }

    public AsyncMono<I> timeout(long timeout) {
        return new AsyncMono<>(input.timeout(Duration.ofMillis(timeout)));
    }

    /**
     * 影响在其之后的 operator执行的线程池
     *
     * @param scheduler
     * @return
     */
    public AsyncMono<I> publishOn(Scheduler scheduler) {
        return new AsyncMono<>(input.publishOn(scheduler));
    }

    /**
     * 源头影响整个执行过程
     *
     * @param scheduler
     * @return
     */
    public AsyncMono<I> subscribeOn(Scheduler scheduler) {
        return new AsyncMono<>(input.subscribeOn(scheduler));
    }

    public void subscribe(Consumer<? super I> success) {
        input.subscribe(new BaseSubscriber<I>() {
            @Override
            protected void hookOnNext(I value) {
                success.accept(value);
            }
        });
    }

    public void subscribe(Consumer<? super I> success, Consumer<? super Throwable> error) {
        input.subscribe(new BaseSubscriber<I>() {
            @Override
            protected void hookOnNext(I value) {
                success.accept(value);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                error.accept(throwable);
            }
        });
    }

    public void subscribe(Subscriber<? super I> actual) {
        input.subscribe(actual);
    }

    public void subscribe(ReactSink<? super I> sink) {
        input.subscribe(new BaseSubscriber<I>() {
            @Override
            protected void hookOnNext(I value) {
                sink.success(value);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                sink.error(throwable);
            }
        });
    }

    public void subscribe() {
        input.subscribe();
    }

}
