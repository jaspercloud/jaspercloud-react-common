package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class HttpTest {

    @Test
    public void test() throws Exception {
        ReactHttpClient reactHttpClient = new ReactHttpClient(1);
        reactHttpClient.execute(new Request.Builder().url("http://www.123baidu.com").build()).timeout(50000).then(new ReactAsyncCall<Response, Response>() {

            private long start;

            @Override
            public void onSubscribe() {
                start = System.currentTimeMillis();
            }

            @Override
            public void onFinally() {
                System.out.println(System.currentTimeMillis() - start);
            }

            @Override
            public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Response> sink) throws Throwable {
                Thread.sleep(1000);
                sink.success(result);
            }
        }).then(new ReactAsyncCall<Response, Void>() {

            private long start;

            @Override
            public void onSubscribe() {
                start = System.currentTimeMillis();
            }

            @Override
            public void onFinally() {
                System.out.println(System.currentTimeMillis() - start);
            }

            @Override
            public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Void> sink) throws Throwable {
                System.out.println();
            }
        }).subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
