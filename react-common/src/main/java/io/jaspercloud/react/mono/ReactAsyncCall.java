package io.jaspercloud.react.mono;

/**
 * 异步处理
 *
 * @param <I>
 * @param <O>
 */
public interface ReactAsyncCall<I, O> {

    default void onSubscribe() {

    }

    default void onFinally() {

    }

    /**
     * 处理
     *
     * @param hasError  是否有异常
     * @param throwable 有异常时，e是数据
     * @param result    没有异常时，in有数据
     * @param sink      异步返回接收器
     * @throws Throwable
     */
    void process(boolean hasError, Throwable throwable, I result, ReactSink<? super O> sink) throws Throwable;
}
