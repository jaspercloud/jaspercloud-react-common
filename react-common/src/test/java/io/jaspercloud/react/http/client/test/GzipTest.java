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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class GzipTest {

    @Test
    public void test() throws Exception {
        Map<String, List<String>> map = new ConcurrentHashMap<>();
        List<String> list = map.computeIfAbsent("test", (s) -> new ArrayList());


        ReactHttpClient reactHttpClient = new ReactHttpClient();
        Request request = new Request.Builder()
                .url("https://v.qq.com")
//                .url("https://www.baidu.com")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .build();
        Response response = reactHttpClient.execute(request).toMono().block();
        String text = new String(response.body().bytes());
        System.out.println();
    }

    @Test
    public void test2() throws Exception {
        new SpringApplicationBuilder(HttpTest.class)
                .web(false)
                .run();

        List<String> urls = new ArrayList<>();
        urls.add("http://www.baidu.com");
        urls.add("https://www.youku.com");
        urls.add("https://www.getui.com");
        urls.add("https://fanyi.baidu.com");
        urls.add("https://ai.taobao.com");
        urls.add("https://www.tmall.com");
        urls.add("https://www.ctrip.com");
        urls.add("https://www.taobao.com");
        urls.add("https://www.iqiyi.com");
        urls.add("https://www.zhihu.com");
//        urls.add("http://172.168.1.132:8090");
//        urls.add("http://172.168.1.132:8091");
//        urls.add("http://172.168.1.132:8092");
//        urls.add("http://172.168.1.17:44116");
//        urls.add("http://fz.tmofamily.com");
//        urls.add("http://admin.fz.tmofamily.com");
//        urls.add("http://redmine.local.tmofamily.com");

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setPoolSize(1000);
        httpConfig.setConnectionTimeout(30 * 1000);
        httpConfig.setReadTimeout(30 * 1000);
        ReactHttpClient reactHttpClient = new ReactHttpClient(httpConfig);
        AtomicLong counter = new AtomicLong();
        while (true) {
            String url = urls.get(RandomUtils.nextInt(0, urls.size()));
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            reactHttpClient.execute(request).then(new ReactAsyncCall<Response, Void>() {
                @Override
                public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super Void> sink) throws Throwable {
                    System.out.println(counter.incrementAndGet());
                    if (hasError) {
                        throw throwable;
                    }
                }
            }).subscribe();
        }
    }
}
