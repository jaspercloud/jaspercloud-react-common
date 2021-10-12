package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.http.server.Async;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactSyncCall;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;

@RestController
@SpringBootApplication
public class TestApplication implements InitializingBean {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, new String[]{
                "--server.port=10080",
                "--server.tomcat.min-spare-threads=100",
                "--server.tomcat.max-threads=100",
//                "--server.tomcat.max-connections=100",
                "--server.tomcat.accept-count=0",
                "--spring.http.multipart.max-file-size=1000MB",
                "--spring.http.multipart.max-request-size=1000MB"
        });
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ReactHttpClient reactHttpClient;

    @Override
    public void afterPropertiesSet() {
        reactHttpClient = new ReactHttpClient(300);
    }

    @GetMapping("/rec/get")
    public byte[] get() throws Exception {
        return "ok".getBytes();
    }

    @PostMapping("/rec/post")
    public byte[] post() throws Exception {
        return "ok".getBytes();
    }

    @PostMapping("/rec/success")
    public byte[] rec(@RequestBody byte[] bytes) throws Exception {
        Thread.sleep(3 * 1000);
        return bytes;
    }

    @PostMapping("/rec/timeout")
    public byte[] recTimeout(@RequestBody byte[] bytes) throws Exception {
        Thread.sleep(30 * 1000);
        return bytes;
    }

    @PostMapping("/rec/file")
    public byte[] recTimeout(MultipartHttpServletRequest request) throws Exception {
        MultipartFile file = request.getFile("test");
        return "test".getBytes();
    }

    @PostMapping("/send/**")
    public DeferredResult<ResponseEntity<String>> send(HttpServletRequest httpServletRequest, @RequestBody byte[] reqBody) throws Exception {
        return Async.run(new Async.AsyncCallable<ResponseEntity<String>>() {
            @Override
            protected Mono<ResponseEntity<String>> call() {
//                RequestBody requestBody = RequestBody.create(MediaType.parse("application/stream"), new File("C:\\Users\\js\\Downloads\\QQ小程序开发者工具_0.2.1_winx64.exe"));
                okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse("application/stream"), new byte[1024 * 1024 * 5]);
                MultipartBody multipartBody = new MultipartBody.Builder()
                        .addFormDataPart("test", "test.amr", requestBody)
                        .build();
                Request request = new Request.Builder()
                        .url("http://127.0.0.1:10080/rec/" + httpServletRequest.getRequestURI().replaceAll("^/send", ""))
                        .header("test", "test")
//                        .post(RequestBody.create(MediaType.parse("application/stream"), reqBody))
//                        .post(RequestBody.create(MediaType.parse("application/stream"), new byte[1024 * 1024 * 10]))
//                        .post(RequestBody.create(MediaType.parse("application/stream"), new byte[1024]))
                        .post(multipartBody)
                        .build();
                Mono<Response> mono = reactHttpClient.execute(request).toMono();

                AsyncMono<String> result = new AsyncMono<>(mono).then(new ReactSyncCall<Response, String>() {
                    @Override
                    public String process(boolean hasError, Throwable throwable, Response in) throws Throwable {
                        if (hasError) {
                            throw throwable;
                        }
                        int code = in.code();
                        if (200 != code) {
                            throw new RuntimeException();
                        }
                        return in.body().string();
                    }
                });
                AsyncMono<ResponseEntity<String>> result2 = new AsyncMono<>(result.toMono()).then(new ReactSyncCall<String, ResponseEntity<String>>() {
                    @Override
                    public ResponseEntity<String> process(boolean hasError, Throwable throwable, String in) throws Throwable {
                        if (hasError) {
                            throw throwable;
                        }
                        return ResponseEntity.ok(in);
                    }
                });
                return result2.toMono();
            }

            @Override
            protected ResponseEntity<String> onThrowable(Throwable e) {
                return ResponseEntity.status(500).body(e.getMessage());
            }

            @Override
            protected void onTimeout() {
                super.onTimeout();
            }
        });
    }
}
