package io.jaspercloud.react.http.server;

import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;

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
        asyncMono.subscribe(new Consumer<T>() {
            @Override
            public void accept(T t) {
                result.setResult(t);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                result.setErrorResult(throwable);
            }
        });
        return result;
    }
}
