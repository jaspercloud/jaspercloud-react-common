package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.HttpConfig;
import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ThreadTest {

    @Test
    public void test() throws Exception {
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

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setPoolSize(3000);
        httpConfig.setConnectionTimeout(30 * 1000);
        httpConfig.setReadTimeout(30 * 1000);
        ReactHttpClient reactHttpClient = new ReactHttpClient(httpConfig);
        int size = 100000;
        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            String url = urls.get(RandomUtils.nextInt(0, urls.size()));
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            reactHttpClient.execute(request).then(new ReactAsyncCall<Response, Void>() {
                @Override
                public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Void> sink) throws Throwable {
                    countDownLatch.countDown();
                    System.out.println(countDownLatch.getCount());
                    if (hasError) {
                        throw throwable;
                    }
                }
            }).subscribe();
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println();
    }

    @Test
    public void test123() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
//            String s1 = "host" + ":" + RandomUtils.nextInt(10, 100);
            String s2 = String.format("%s:%s", "host", RandomUtils.nextInt(10, 100));
        }
        long end = System.currentTimeMillis();
        long diff = end - start;
        //417
        //25
        System.out.println(diff);
    }
}
