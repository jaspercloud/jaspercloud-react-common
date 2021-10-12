package io.jaspercloud.react.http.client;

import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.function.Consumer;

public class ProcessRequestBody implements Consumer<FluxSink<byte[]>> {

    private Request request;

    public ProcessRequestBody(Request request) {
        this.request = request;
    }

    @Override
    public void accept(FluxSink<byte[]> sink) {
        try {
            RequestBody requestBody = request.body();
            if (null == requestBody) {
                return;
            }
            BufferedSink buffer = Okio.buffer(Okio.sink(new OutputStream() {
                @Override
                public void write(byte[] b) throws IOException {
                    sink.next(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    sink.next(Arrays.copyOfRange(b, off, len));
                }

                @Override
                public void write(int b) throws IOException {
                    sink.next(new byte[]{(byte) b});
                }
            }));
            requestBody.writeTo(buffer);
            buffer.flush();
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.complete();
        }
    }
}
