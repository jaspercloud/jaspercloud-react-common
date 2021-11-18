package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.HttpConfig;
import io.jaspercloud.react.http.client.ReactHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;

public class ReactAllTest {

    private ReactHttpClient reactHttpClient;

    @Before
    public void init() {
        reactHttpClient = new ReactHttpClient(new HttpConfig()
                .setConnectionTimeout(100 * 1000)
                .setWriteTimeout(5 * 1000)
                .setReadTimeout(100 * 1000));
    }

    @Test
    public void get() {
        Response response = reactHttpClient.execute(new Request.Builder()
                .url("http://172.168.1.132:888/test")
                .build()).toMono().block();
        System.out.println();
    }

    @Test
    public void post() {
        Response response = reactHttpClient.execute(new Request.Builder()
                .url("http://172.168.1.132:888/test")
                .post(RequestBody.create(null, "test"))
                .build()).toMono().block();
        System.out.println();
    }

    @Test
    public void put() {
        Response response = reactHttpClient.execute(new Request.Builder()
                .url("http://172.168.1.132:888/test")
                .put(RequestBody.create(null, "test"))
                .build()).toMono().block();
        System.out.println();
    }

    @Test
    public void delete() {
        Response response = reactHttpClient.execute(new Request.Builder()
                .url("http://172.168.1.132:888/test")
                .delete(RequestBody.create(null, "test"))
                .build()).toMono().block();
        System.out.println();
    }
}
