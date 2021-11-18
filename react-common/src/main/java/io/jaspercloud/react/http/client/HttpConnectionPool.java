package io.jaspercloud.react.http.client;

import io.jaspercloud.react.mono.AsyncMono;

public interface HttpConnectionPool {

    AsyncMono<HttpConnection> acquire(String host, int port, long timeout);

    void release(HttpConnection connection);

    interface HttpConnectionCreate {

        HttpConnection create();
    }
}
