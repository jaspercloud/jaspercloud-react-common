package io.jaspercloud.react.mono;

public interface ReactSink<T> {

    void success(T t);

    void error(Throwable throwable);
}
