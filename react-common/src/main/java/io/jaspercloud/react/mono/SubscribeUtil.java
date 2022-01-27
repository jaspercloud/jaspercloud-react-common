package io.jaspercloud.react.mono;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SignalType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class SubscribeUtil {

    private SubscribeUtil() {

    }

    public static <I, O> Mono<StreamRecord<O>> processStreamRecordMono(Mono<StreamRecord<I>> mono, ReactAsyncCall<I, O> call) {
        Mono<StreamRecord<O>> result = Mono.create(new Consumer<MonoSink<StreamRecord<O>>>() {
            @Override
            public void accept(MonoSink<StreamRecord<O>> sink) {
                DefaultReactSink reactSink = new DefaultReactSink(sink);
                mono.subscribe(new BaseSubscriber<StreamRecord<I>>() {
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
        return result;
    }

    public static <I> Mono<StreamRecord<I>> subscribeMono(Mono<I> mono) {
        Mono<StreamRecord<I>> result = Mono.create(new Consumer<MonoSink<StreamRecord<I>>>() {
            @Override
            public void accept(MonoSink<StreamRecord<I>> sink) {
                AtomicBoolean status = new AtomicBoolean(false);
                mono.subscribe(new BaseSubscriber<I>() {

                    private I value;
                    private Throwable throwable;

                    @Override
                    protected void hookOnNext(I value) {
                        if (status.compareAndSet(false, true)) {
                            this.value = value;
                            sink.success(new StreamRecord<>(value));
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        if (status.compareAndSet(false, true)) {
                            this.throwable = throwable;
                            sink.error(throwable);
                        }
                    }

                    @Override
                    protected void hookFinally(SignalType type) {
                        if (status.compareAndSet(false, true)) {
                            if (null == throwable) {
                                sink.success(new StreamRecord<>(value));
                            } else {
                                sink.error(throwable);
                            }
                        }
                    }
                });
            }
        });
        return result;
    }
}
