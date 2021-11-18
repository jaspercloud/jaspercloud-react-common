package io.jaspercloud.react.mono.invoke;

import io.jaspercloud.react.mono.AsyncMono;

public class InvokeRequest<K, R> {

    private K key;
    private AsyncMono<R> route;

    public K getKey() {
        return key;
    }

    public AsyncMono<R> getRoute() {
        return route;
    }

    public InvokeRequest(K key, AsyncMono<R> route) {
        this.key = key;
        this.route = route;
    }
}