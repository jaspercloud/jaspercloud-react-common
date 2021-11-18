package io.jaspercloud.react.mono;

import reactor.core.publisher.MonoSink;

public class DefaultReactSink<O> implements ReactSink<O> {

    private MonoSink<O> sink;
    private O result;
    private Throwable throwable;

    public void setResult(O result) {
        this.result = result;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public DefaultReactSink(MonoSink<O> sink) {
        this.sink = sink;
    }

    @Override
    public void success(O o) {
        sink.success(o);
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
            sink.success(result);
        }
    }
}
