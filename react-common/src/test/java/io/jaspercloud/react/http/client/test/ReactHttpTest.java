package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.ReactHttpClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;

public class ReactHttpTest {

    public static void main(String[] args) throws Exception {
        ReactHttpClient reactHttpClient = new ReactHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/stream"), "content".getBytes());
//        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse("application/stream"), new File("C:\\Users\\js\\Downloads\\QQ小程序开发者工具_0.2.1_winx64.exe"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .addFormDataPart("test", "test.amr", requestBody)
                .build();
        Request request = new Request.Builder()
                .url("http://127.0.0.1:10080/rec/file")
                .post(multipartBody)
                .build();
        for (int i = 0; i < 1000000; i++) {
            Mono<Response> mono = reactHttpClient.execute(request).toMono();
            mono.subscribe(res -> {
                if (200 == res.code()) {
                    return;
                }
                System.out.println();
            }, err -> {
                System.out.println(err.getMessage());
            });
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
