package io.jaspercloud.react.mono;

/**
 * 同步处理
 *
 * @param <I>
 * @param <O>
 */
public abstract class ReactSyncCall<I, O> implements ReactAsyncCall<I, O> {

    /**
     * 处理
     *
     * @param hasError  是否有异常
     * @param throwable 有异常时，e是数据
     * @param result    没有异常时，in有数据
     * @return
     * @throws Throwable
     */
    protected abstract O process(boolean hasError, Throwable throwable, I result) throws Throwable;

    @Override
    public void process(boolean hasError, Throwable throwable, I result, ReactSink<? super O> sink) throws Throwable {
        try {
            O ret = process(hasError, throwable, result);
            sink.success(ret);
        } catch (Throwable t) {
            sink.error(t);
        }
    }
}
