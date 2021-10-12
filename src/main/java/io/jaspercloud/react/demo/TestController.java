package io.jaspercloud.react.demo;

import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.http.server.Async;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import io.jaspercloud.react.mono.ReactSyncCall;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@RestController
public class TestController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Configuration
    public static class AppConfig {

        @Bean
        public ReactHttpClient reactHttpClient() {
            return new ReactHttpClient(300);
        }
    }

    @Autowired
    private ReactHttpClient reactHttpClient;

    @GetMapping("/test")
    public void test(HttpServletRequest request, HttpServletResponse response) {
        Async.start(request, response, new Async.AsyncCall() {
            @Override
            public void onCall(Async.AsyncSession session) {
                session.complete(ResponseEntity.status(200).build());
            }
        });
    }

    @PostMapping
    public DeferredResult<ResponseEntity<String>> test1(@RequestBody byte[] bytes) throws Exception {
        return Async.run(5 * 1000L, new Async.AsyncCallable<ResponseEntity<String>>() {
            @Override
            protected Mono<ResponseEntity<String>> call() {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .post(okhttp3.RequestBody.create(MediaType.parse("application/stream"), bytes))
                        .build();
                AsyncMono<ResponseEntity<String>> result = reactHttpClient.execute(request, 10 * 1000).then(new ReactSyncCall<Response, ResponseEntity<String>>() {
                    /**
                     * 用于同步数据返回
                     * @param hasError 是否有异常
                     * @param throwable        有异常时，throwable是数据
                     * @param result       没有异常时，result有数据
                     * @return
                     * @throws Throwable
                     */
                    @Override
                    protected ResponseEntity<String> process(boolean hasError, Throwable throwable, Response result) throws Throwable {
                        if (hasError) {
                            logger.error(throwable.getMessage());
                            return ResponseEntity.status(500).body(throwable.getMessage());
                        }
                        int code = result.code();
                        String resp = result.body().string();
                        if (200 != code) {
                            logger.error("code={}, body={}", code, resp);
                            return ResponseEntity.status(code).body(resp);
                        }
                        return ResponseEntity.ok(resp);
                    }
                });
                return result.toMono();
            }

            @Override
            protected ResponseEntity<String> onThrowable(Throwable e) {
                logger.error("request exception: {}", e.getMessage());
                return ResponseEntity.status(500).body(e.getMessage());
            }

            @Override
            protected void onTimeout() {
                logger.error("request onTimeout");
            }
        });
    }

    @PostMapping
    public DeferredResult<ResponseEntity<String>> test2(@RequestBody byte[] bytes) throws Exception {
        return Async.run(5 * 1000L, new Async.AsyncCallable<ResponseEntity<String>>() {
            @Override
            protected Mono<ResponseEntity<String>> call() {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .post(okhttp3.RequestBody.create(MediaType.parse("application/stream"), bytes))
                        .build();
                AsyncMono<ResponseEntity<String>> result = reactHttpClient.execute(request, 10 * 1000).then(new ReactAsyncCall<Response, ResponseEntity<String>>() {
                    /**
                     * 用于异步处理返回
                     * @param hasError 是否有异常
                     * @param throwable        有异常时，e是数据
                     * @param result       没有异常时，in有数据
                     * @param sink   异步返回接收器
                     * @throws Throwable
                     */
                    @Override
                    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super ResponseEntity<String>> sink) throws Throwable {
                        if (hasError) {
                            logger.error(throwable.getMessage());
                            sink.success(ResponseEntity.status(500).body(throwable.getMessage()));
                            return;
                        }
                        int code = result.code();
                        String resp = result.body().string();
                        if (200 != code) {
                            logger.error("code={}, body={}", code, resp);
                            sink.success(ResponseEntity.status(code).body(resp));
                            return;
                        }
                        sink.success(ResponseEntity.ok(resp));
                    }
                });
                return result.toMono();
            }

            @Override
            protected ResponseEntity<String> onThrowable(Throwable e) {
                logger.error("request exception: {}", e.getMessage());
                return ResponseEntity.status(500).body(e.getMessage());
            }

            @Override
            protected void onTimeout() {
                logger.error("request onTimeout");
            }
        });
    }
}
