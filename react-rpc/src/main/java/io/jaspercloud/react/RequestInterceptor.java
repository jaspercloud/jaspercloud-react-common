package io.jaspercloud.react;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.template.ReturnTemplate;
import okhttp3.Request;
import okhttp3.Response;

public interface RequestInterceptor {

    default String convertParam(String name, Object value) {
        return null;
    }

    default AsyncMono<Request> onRequest(Request request, Chain<Request> chain) {
        return chain.proceed(request);
    }

    default AsyncMono<Response> onResponse(Response response, Chain<Response> chain) {
        return chain.proceed(response);
    }

    default Object onReturn(ReturnTemplate returnTemplate, AsyncMono<Object> asyncMono) {
        return null;
    }

    interface Chain<T> {

        AsyncMono<T> proceed(T result);
    }
}
