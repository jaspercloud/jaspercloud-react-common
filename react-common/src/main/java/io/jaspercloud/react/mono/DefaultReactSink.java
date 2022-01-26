package io.jaspercloud.react.mono;

import reactor.core.publisher.MonoSink;

public class DefaultReactSink<O> implements ReactSink<O> {

    private MonoSink<StreamRecord<O>> sink;
    private O result;
    private Throwable throwable;

    public O getResult() {
        return result;
    }

    public void setResult(O result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public DefaultReactSink(MonoSink<StreamRecord<O>> sink) {
        this.sink = sink;
    }

    @Override
    public void success() {
        sink.success(new StreamRecord<>());
    }

    @Override
    public void success(O o) {
        sink.success(new StreamRecord<>(o));
    }

    @Override
    public void error(Throwable throwable) {
        sink.error(throwable);
    }

    @Override
    public void finish() {
        if (null != throwable) {
            sink.error(throwable);
        } else {
            sink.success(new StreamRecord<>(result));
        }
    }
}
