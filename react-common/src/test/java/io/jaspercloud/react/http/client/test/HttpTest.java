package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.HttpConfig;
import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@SpringBootApplication
public class HttpTest {

    @Test
    public void okhttp() throws Exception {
        new SpringApplicationBuilder(HttpTest.class)
                .web(false)
                .run();

        List<String> urls = new ArrayList<>();
        urls.add("https://www.youku.com");
        urls.add("https://www.getui.com");
        urls.add("https://fanyi.baidu.com");
        urls.add("https://ai.taobao.com");
        urls.add("https://www.tmall.com");
        urls.add("https://www.ctrip.com");
        urls.add("https://www.taobao.com");
        urls.add("https://www.iqiyi.com");
        urls.add("https://www.zhihu.com");

        OkHttpClient okHttpClient = new OkHttpClient();
        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(urls.size());
        for (String url : urls) {
            System.out.println(url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            countDownLatch.countDown();
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println();
    }

//    @Test
//    public void httpClient() throws InterruptedException {
//        new SpringApplicationBuilder(HttpTest.class)
//                .web(false)
//                .run();
//
//        List<String> urls = new ArrayList<>();
//        urls.add("https://www.youku.com");
//        urls.add("https://www.getui.com");
//        urls.add("https://fanyi.baidu.com");
//        urls.add("https://ai.taobao.com");
//        urls.add("https://www.tmall.com");
//        urls.add("https://www.ctrip.com");
//        urls.add("https://www.taobao.com");
//        urls.add("https://www.iqiyi.com");
//        urls.add("https://www.zhihu.com");
//
//        HttpClient httpClient = HttpClient.create(ConnectionProvider.newConnection());
//        long start = System.currentTimeMillis();
//        CountDownLatch countDownLatch = new CountDownLatch(urls.size());
//        for (String url : urls) {
//            System.out.println(url);
//            httpClient.get().uri(url).responseSingle(new BiFunction<HttpClientResponse, ByteBufMono, Mono<byte[]>>() {
//                @Override
//                public Mono<byte[]> apply(HttpClientResponse response, ByteBufMono byteBufMono) {
//                    return Mono.create(sink -> {
//                        byteBufMono.subscribe(new Consumer<ByteBuf>() {
//                            @Override
//                            public void accept(ByteBuf byteBuf) {
//                                byte[] bytes = new byte[byteBuf.readableBytes()];
//                                byteBuf.readBytes(bytes);
//                                sink.success(bytes);
//                            }
//                        });
//                    });
//                }
//            }).block();
//            countDownLatch.countDown();
//        }
//        countDownLatch.await();
//        long end = System.currentTimeMillis();
//        long diff = end - start;
//        System.out.println();
//    }

    @Test
    public void netty() throws Exception {
        new SpringApplicationBuilder(HttpTest.class)
                .web(false)
                .run();

        List<String> urls = new ArrayList<>();
//        urls.add("http://www.baidu.com");
//        urls.add("https://www.youku.com");
//        urls.add("https://www.getui.com");
//        urls.add("https://fanyi.baidu.com");
//        urls.add("https://ai.taobao.com");
//        urls.add("https://www.tmall.com");
//        urls.add("https://www.ctrip.com");
//        urls.add("https://www.taobao.com");
//        urls.add("https://www.iqiyi.com");
//        urls.add("https://www.zhihu.com");
        urls.add("http://172.168.1.132:8090");
        urls.add("http://172.168.1.132:8091");
        urls.add("http://172.168.1.132:8092");
//        urls.add("http://172.168.1.17:44116");
        urls.add("http://fz.tmofamily.com");
        urls.add("http://admin.fz.tmofamily.com");
        urls.add("http://redmine.local.tmofamily.com");

        AtomicLong counter = new AtomicLong();
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(counter.getAndSet(0));
                    }
                }, 0, 1000, TimeUnit.MILLISECONDS);

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setPoolSize(1000);
        httpConfig.setConnectionTimeout(5 * 1000);
        httpConfig.setReadTimeout(5 * 1000);
        ReactHttpClient reactHttpClient = new ReactHttpClient(httpConfig);
        while (true) {
            CountDownLatch countDownLatch = new CountDownLatch(httpConfig.getPoolSize()+100);
            for (int i = 0; i < httpConfig.getPoolSize(); i++) {
                String url = urls.get(RandomUtils.nextInt(0, urls.size()));
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                reactHttpClient.execute(request).then(new ReactAsyncCall<Response, Void>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Void> sink) throws Throwable {
                        counter.incrementAndGet();
                        countDownLatch.countDown();
                        if (hasError) {
                            throw throwable;
                        }
                    }
                }).subscribe();
            }
            countDownLatch.await();
        }
    }

    @Test
    public void nettyNative() throws Exception {
        new SpringApplicationBuilder(HttpTest.class)
                .web(false)
                .run();

        List<String> urls = new ArrayList<>();
        urls.add("https://www.youku.com");
        urls.add("https://www.getui.com");
        urls.add("https://fanyi.baidu.com");
        urls.add("https://ai.taobao.com");
        urls.add("https://www.tmall.com");
        urls.add("https://www.ctrip.com");
        urls.add("https://www.taobao.com");
        urls.add("https://www.iqiyi.com");
        urls.add("https://www.zhihu.com");


        NettyClient nettyClient = new NettyClient();
        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(urls.size());
        for (String url : urls) {
            System.out.println(url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            FullHttpResponse response = nettyClient.execute(request);
            countDownLatch.countDown();
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println();
    }


    @Test
    public void test() throws Exception {
        ReactHttpClient reactHttpClient = new ReactHttpClient();
        reactHttpClient.execute(new Request.Builder().url("http://www.baidu.com").build()).timeout(50000).then(new ReactAsyncCall<Response, String>() {

            private long start;

            @Override
            public void onSubscribe() {
                start = System.currentTimeMillis();
            }

            @Override
            public void onFinally() {
                System.out.println("runTime: " + (System.currentTimeMillis() - start));
            }

            @Override
            public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super String> sink) throws Throwable {
                if (hasError) {
                    throw throwable;
                }
                try (Response response = result) {
                    sink.success(response.body().string());
                }
            }
        }).then(new ReactAsyncCall<String, Void>() {

            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super Void> sink) throws Throwable {
                System.out.println(result);
            }
        }).subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
