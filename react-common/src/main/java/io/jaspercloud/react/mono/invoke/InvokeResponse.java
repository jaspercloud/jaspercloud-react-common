package io.jaspercloud.react.mono.invoke;

public class InvokeResponse<K, R> {

    private K key;
    private R result;
    private Throwable throwable;

    public K getKey() {
        return key;
    }

    public R getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public InvokeResponse(K key, R result) {
        this.key = key;
        this.result = result;
    }

    public InvokeResponse(K key, Throwable throwable) {
        this.key = key;
        this.throwable = throwable;
    }
}