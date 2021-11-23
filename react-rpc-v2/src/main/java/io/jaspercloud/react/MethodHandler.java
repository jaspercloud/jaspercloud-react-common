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
        AsyncMono<Object> asyncMono = requestTemplate.getInterceptorAdapter().onRequest(request)
                .then(new ReactAsyncCall<Request, Response>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, Request request, ReactSink<? super Response> sink) throws Throwable {
                        if (hasError) {
                            sink.finish();
                            return;
                        }
                        reactHttpClient.execute(request).subscribe(sink);
                    }
                })
                .then(new ReactAsyncCall<Response, Object>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, Response response, ReactSink<? super Object> sink) throws Throwable {
                        if (hasError) {
                            sink.finish();
                            return;
                        }
                        requestTemplate.getInterceptorAdapter().onResponse(response)
                                .then(new ReactAsyncCall<Response, Object>() {
                                    @Override
                                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Object> sink) throws Throwable {
                                        try {
                                            Object responseBody = requestTemplate.convertResponseBody(response);
                                            sink.success(responseBody);
                                        } finally {
                                            response.close();
                                        }
                                    }
                                }).subscribe(sink);
                    }
                });
        Object result = requestTemplate.getInterceptorAdapter().onReturn(requestTemplate.getReturnTemplate(), asyncMono);
        if (null != result) {
            return result;
        }
        Class<?> returnClass = requestTemplate.getReturnTemplate().getReturnClass();
        if (CompletableFuture.class.isAssignableFrom(returnClass)) {
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
        } else if (AsyncMono.class.isAssignableFrom(returnClass)) {
            return asyncMono;
        } else if (Mono.class.isAssignableFrom(returnClass)) {
            return asyncMono.toMono();
        } else {
            Object response = asyncMono.toMono().block();
            return response;
        }
    }
}
