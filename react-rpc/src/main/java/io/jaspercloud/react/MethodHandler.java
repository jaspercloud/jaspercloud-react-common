package io.jaspercloud.react;

import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.jaspercloud.react.template.RequestTemplate;
import okhttp3.Request;
import okhttp3.Response;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public class MethodHandler {

    private ReactHttpClient reactHttpClient;
    private RequestTemplate requestTemplate;

    public MethodHandler(ReactHttpClient reactHttpClient, RequestTemplate requestTemplate) {
        this.reactHttpClient = reactHttpClient;
        this.requestTemplate = requestTemplate;
    }

    public Object invoke(Object[] args) {
        Request request = requestTemplate.buildRequest(args);
        request = requestTemplate.getInterceptorAdapter().onRequest(request);
        AsyncMono<Object> asyncMono = reactHttpClient.execute(request)
                .then(new ReactAsyncCall<Response, Object>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, Response response, ReactSink<? super Object> sink) throws Throwable {
                        try {
                            if (hasError) {
                                sink.finish();
                                return;
                            }
                            response = requestTemplate.getInterceptorAdapter().onResponse(response);
                            Object result = requestTemplate.convertResponseBody(response);
                            sink.success(result);
                        } finally {
                            response.close();
                        }
                    }
                });
        if (CompletableFuture.class.isAssignableFrom(requestTemplate.getReturnTemplate().getReturnClass())) {
            CompletableFuture future = new CompletableFuture();
            asyncMono.subscribe(new BaseSubscriber<Object>() {
                @Override
                protected void hookOnNext(Object response) {
                    future.complete(response);
                }

                @Override
                protected void hookOnError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
            return future;
        } else if (AsyncMono.class.isAssignableFrom(requestTemplate.getReturnTemplate().getReturnClass())) {
            return asyncMono;
        } else if (Mono.class.isAssignableFrom(requestTemplate.getReturnTemplate().getReturnClass())) {
            return asyncMono.toMono();
        } else {
            Object response = asyncMono.toMono().block();
            return response;
        }
    }
}
