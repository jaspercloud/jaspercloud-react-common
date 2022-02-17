package io.jaspercloud.react.http.client.rejected;

import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.AsyncMono;
import okhttp3.Request;
import okhttp3.Response;

public interface RejectedExecutionHandler {

    AsyncMono<Response> rejectedExecution(Request request, ReactHttpClient httpClient);
}
