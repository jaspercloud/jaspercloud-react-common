package io.jaspercloud.react.mono;

public class StreamRecord<T> {

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public StreamRecord() {

    }

    public StreamRecord(T data) {
        this.data = data;
    }
}
