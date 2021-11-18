package io.jaspercloud.react.http.server;

import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.BaseSubscriber;

/**
 * SpringMVC 异步处理
 */
public class AsyncResult<T> {

    private AsyncMono<T> asyncMono;

    public AsyncResult(AsyncMono<T> asyncMono) {
        this.asyncMono = asyncMono;
    }

    public DeferredResult toResult(long timeout) {
        DeferredResult<T> result = new DeferredResult<>(timeout);
        asyncMono.subscribe(new BaseSubscriber<T>() {
            @Override
            protected void hookOnNext(T value) {
                result.setResult(value);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                result.setErrorResult(throwable);
            }
        });
        return result;
    }
}
