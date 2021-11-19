package io.jaspercloud.react;

import okhttp3.Request;
import okhttp3.Response;

public interface RequestInterceptor {

    String convertParam(String name, Object value);

    Request onRequest(Request request, Chain<Request> chain);

    Response onResponse(Response response, Chain<Response> chain);

    interface Chain<T> {

        T proceed(T result);
    }
}
