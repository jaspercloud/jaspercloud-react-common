package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;
import io.netty.handler.codec.http.HttpMethod;
import okhttp3.Request;
import okhttp3.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * ReactHttpClient
 * 用于异步高并发请求处理
 */
public class ReactHttpClient {

    public ReactHttpClient() {
    }

    /**
     * 请求
     *
     * @param request
     * @return
     */
    public AsyncMono<Response> execute(Request request) {
        Mono<Response> mono = HttpClient.create(ConnectionProvider.newConnection())
                .keepAlive(true)
                .headers(new ProcessHeader(request))
                .request(HttpMethod.valueOf(request.method()))
                .uri(request.url().toString())
                .send((req, out) -> {
                    return out.sendByteArray(Flux.create(new ProcessRequestBody(request)));
                })
                .responseSingle(new ProcessResponse(request));
        return new AsyncMono<>(mono);
    }

    /**
     * 请求
     *
     * @param request
     * @param timeout 超时时间
     * @return
     */
    public AsyncMono<Response> execute(Request request, long timeout) {
        return execute(request).timeout(timeout);
    }
}
