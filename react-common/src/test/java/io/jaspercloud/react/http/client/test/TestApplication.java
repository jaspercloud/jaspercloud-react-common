//package io.jaspercloud.react.http.client.test;
//
//import io.jaspercloud.react.http.client.ReactHttpClient;
//import io.jaspercloud.react.mono.AsyncMono;
//import io.jaspercloud.react.mono.ReactSyncCall;
//import okhttp3.Request;
//import okhttp3.Response;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.context.request.async.DeferredResult;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.MultipartHttpServletRequest;
//import reactor.core.publisher.Mono;
//
//@RestController
//@SpringBootApplication
//public class TestApplication implements InitializingBean {
//
//    public static void main(String[] args) {
//        SpringApplication.run(TestApplication.class, new String[]{
//                "--server.port=10080",
//                "--server.tomcat.min-spare-threads=100",
//                "--server.tomcat.max-threads=100",
////                "--server.tomcat.max-connections=100",
//                "--server.tomcat.accept-count=0",
//                "--spring.http.multipart.max-file-size=1000MB",
//                "--spring.http.multipart.max-request-size=1000MB"
//        });
//    }
//
//    private Logger logger = LoggerFactory.getLogger(getClass());
//
//    private ReactHttpClient reactHttpClient;
//
//    @Override
//    public void afterPropertiesSet() {
//        reactHttpClient = new ReactHttpClient();
//    }
//
//    @GetMapping("/rec/get")
//    public byte[] get() throws Exception {
//        return "ok".getBytes();
//    }
//
//    @PostMapping("/rec/post")
//    public byte[] post() throws Exception {
//        return "ok".getBytes();
//    }
//
//    @PostMapping("/rec/success")
//    public byte[] rec(@RequestBody byte[] bytes) throws Exception {
//        Thread.sleep(3 * 1000);
//        return bytes;
//    }
//
//    @PostMapping("/rec/timeout")
//    public byte[] recTimeout(@RequestBody byte[] bytes) throws Exception {
//        Thread.sleep(30 * 1000);
//        return bytes;
//    }
//
//    @PostMapping("/rec/file")
//    public byte[] recTimeout(MultipartHttpServletRequest request) throws Exception {
//        MultipartFile file = request.getFile("test");
//        return "test".getBytes();
//    }
//
//    @GetMapping("/test")
//    public DeferredResult<ResponseEntity<String>> test() throws Exception {
//        return AsyncResult.run(new AsyncResult.AsyncCallable<ResponseEntity<String>>() {
//            @Override
//            protected Mono<ResponseEntity<String>> call() {
//                Request request = new Request.Builder()
//                        .url("http://www.baidu.com")
//                        .build();
//                //请求
//                Mono<Response> mono = reactHttpClient.execute(request).toMono();
//                AsyncMono<ResponseEntity<String>> result = new AsyncMono<>(mono).then(new ReactSyncCall<Response, ResponseEntity<String>>() {
//                    @Override
//                    public ResponseEntity<String> process(boolean hasError, Throwable throwable, Response in) throws Throwable {
//                        //Response处理
//                        if (hasError) {
//                            throw throwable;
//                        }
//                        int code = in.code();
//                        if (200 != code) {
//                            throw new RuntimeException();
//                        }
//                        return ResponseEntity.ok(in.body().string());
//                    }
//                });
//                return result.toMono();
//            }
//
//            @Override
//            protected ResponseEntity<String> onThrowable(Throwable e) {
//                return ResponseEntity.status(500).body(e.getMessage());
//            }
//
//            @Override
//            protected void onTimeout() {
//                super.onTimeout();
//            }
//        });
//    }
//}
