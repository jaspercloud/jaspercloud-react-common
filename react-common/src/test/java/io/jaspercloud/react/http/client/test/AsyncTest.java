package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.HttpConfig;
import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class AsyncTest {

    @Test
    public void test() throws Exception {
        new SpringApplicationBuilder(AsyncTest.class)
                .web(false)
                .run();

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setPoolSize(1000);
        httpConfig.setConnectionTimeout(30 * 1000);
        httpConfig.setReadTimeout(30 * 1000);
        ReactHttpClient reactHttpClient = new ReactHttpClient(httpConfig);
        reactHttpClient.execute(new Request.Builder().url("http://www.baidu.com").build())
                .then(new ReactAsyncCall<Response, Response>() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 1);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 1);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Response> sink) throws Throwable {
                        System.out.println("process" + 1);
                        sink.success(result);
                    }
                })
                .then(new ReactAsyncCall<Response, Response>() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 2);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 2);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Response> sink) throws Throwable {
                        System.out.println("process" + 2);
                        sink.success(result);
                    }
                })
                .then(new ReactAsyncCall<Response, Response>() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 3);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 3);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Response> sink) throws Throwable {
                        System.out.println("process" + 3);
                        sink.success(result);
                    }
                })
                .then(new ReactAsyncCall<Response, Response>() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 4);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 4);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Response> sink) throws Throwable {
                        System.out.println("process" + 4);
                        sink.success(result);
                    }
                })
                .subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void test2() throws Exception {
        Mono mono = new AsyncMono(Mono.just("test"))
                .then(new ReactAsyncCall() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 1);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 1);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Object result, ReactSink sink) throws Throwable {
                        sink.success("test");
                    }
                })
                .then(new ReactAsyncCall() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 2);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 2);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Object result, ReactSink sink) throws Throwable {
                        sink.error(new TimeoutException());
                    }
                })
                .then(new ReactAsyncCall() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 3);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 3);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Object result, ReactSink sink) throws Throwable {
                        sink.success("test");
                    }
                }).toMono();
        new AsyncMono<>(mono)
                .then(new ReactAsyncCall() {
                    @Override
                    public void onSubscribe() {
                        System.out.println("onSubscribe" + 4);
                    }

                    @Override
                    public void onFinally() {
                        System.out.println("onFinally" + 4);
                    }

                    @Override
                    public void process(boolean hasError, Throwable throwable, Object result, ReactSink sink) throws Throwable {
                        sink.success("test");
                    }
                }).subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
