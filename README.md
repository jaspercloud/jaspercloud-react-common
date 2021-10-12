# jaspercloud-react-common
## react-http-client
``` java
ReactHttpClient reactHttpClient = new ReactHttpClient(1);
reactHttpClient.execute(new Request.Builder()
.url("http://www.baidu.com")
.build())
.timeout(3000)
.then(new ReactAsyncCall<Response, String>() {

    private long start;

    @Override
    public void onSubscribe() {
        start = System.currentTimeMillis();
    }

    @Override
    public void onFinally() {
        //记录请求时间
        System.out.println("runTime: " + (System.currentTimeMillis() - start));
    }

    @Override
    public void process(boolean hasError, Throwable throwable, Response result, ReactSink<? super String> sink) throws Throwable {
        //Response处理
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
        //链式处理
        System.out.println(result);
    }
}).subscribe();
```
## Springboot Controller中使用
``` java
@GetMapping("/test")
public DeferredResult<ResponseEntity<String>> test() throws Exception {
    return Async.run(new Async.AsyncCallable<ResponseEntity<String>>() {
        @Override
        protected Mono<ResponseEntity<String>> call() {
            Request request = new Request.Builder()
                    .url("http://www.baidu.com")
                    .build();
            //请求
            Mono<Response> mono = reactHttpClient.execute(request).toMono();
            AsyncMono<ResponseEntity<String>> result = new AsyncMono<>(mono).then(new ReactSyncCall<Response, ResponseEntity<String>>() {
                @Override
                public ResponseEntity<String> process(boolean hasError, Throwable throwable, Response in) throws Throwable {
                    //Response处理
                    if (hasError) {
                        throw throwable;
                    }
                    int code = in.code();
                    if (200 != code) {
                        throw new RuntimeException();
                    }
                    return ResponseEntity.ok(in.body().string());
                }
            });
            return result.toMono();
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
```
