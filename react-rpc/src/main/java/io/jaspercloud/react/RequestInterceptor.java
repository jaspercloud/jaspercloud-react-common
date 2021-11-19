package io.jaspercloud.react;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.template.ReturnTemplate;
import okhttp3.Request;
import okhttp3.Response;

public interface RequestInterceptor {

    String convertParam(String name, Object value);

    Request onRequest(Request request, Chain<Request> chain);

    Response onResponse(Response response, Chain<Response> chain);

    Object onReturn(ReturnTemplate returnTemplate, AsyncMono<Object> asyncMono);

    interface Chain<T> {

        T proceed(T result);
    }
}
