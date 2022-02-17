package io.jaspercloud.react.http.client.rejected;

import io.jaspercloud.react.exception.ReactException;
import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.AsyncMono;
import okhttp3.Request;
import okhttp3.Response;

public class AbortPolicy implements RejectedExecutionHandler {

    @Override
    public AsyncMono<Response> rejectedExecution(Request request, ReactHttpClient httpClient) {
        return new AsyncMono<>(new ReactException("concurrent limit"));
    }
}
