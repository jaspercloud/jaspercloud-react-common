package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.ReactHttpClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ReactHttpClientTest {

    @Test
    public void test() throws Exception {
        ReactHttpClient reactHttpClient = new ReactHttpClient(4);
        Request reactRequest = new Request.Builder()
                .url("http://127.0.0.1:4840/video/api/test2")
                .build();
        reactHttpClient.execute(reactRequest).toMono().subscribe(new Consumer<Response>() {
            @Override
            public void accept(Response reactResponse) {
                try {
                    byte[] bytes = reactResponse.body().bytes();
                    System.out.println();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void upload() throws Exception {
        ReactHttpClient reactHttpClient = new ReactHttpClient(4);
        RequestBody reactRequestBody = MultipartBody.create(MediaType.parse("application/octet-stream"), new File("E:\\SVN\\common_service\\react-common\\pom.xml"));
        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("file", "test", reactRequestBody)
                .build();
        Request reactRequest = new Request.Builder()
                .url("http://127.0.0.1:4840/video/api/upload")
                .post(requestBody)
                .build();
        reactHttpClient.execute(reactRequest).toMono().subscribe(new Consumer<Response>() {
            @Override
            public void accept(Response reactResponse) {
                try {
                    byte[] bytes = reactResponse.body().bytes();
                    System.out.println();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void gzip() throws Exception {
        ReactHttpClient reactHttpClient = new ReactHttpClient(4);
        Request reactRequest = new Request.Builder()
                .url("https://www.baidu.com/")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .addHeader("Host", "www.baidu.com")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                .build();
        reactHttpClient.execute(reactRequest).toMono().subscribe(new Consumer<Response>() {
            @Override
            public void accept(Response reactResponse) {
                try {
                    byte[] bytes = reactResponse.body().bytes();
                    System.out.println();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
