## react-http-client
``` xml
<dependency>
    <groupId>io.github.jaspercloud</groupId>
    <artifactId>react-common</artifactId>
    <version>3.0.15</version>
</dependency>
```
``` java
ReactHttpClient reactHttpClient = new ReactHttpClient();
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
            sink.error(throwable);
            return;
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
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        //请求
        Mono<Response> mono = reactHttpClient.execute(request).toMono();
        AsyncMono<ResponseEntity<String>> result = new AsyncMono<>(mono).then(new ReactAsyncCall<Response, ResponseEntity<String>>() {
            @Override
            public void process(boolean hasError, Throwable throwable, Response response, ReactSink<? super ResponseEntity<String>> sink) throws Throwable {
                //Response处理
                if (hasError) {
                    sink.finish();
                    return;
                }
                int code = response.code();
                if (200 != code) {
                    throw new RuntimeException();
                }
                sink.success(ResponseEntity.ok(response.body().string()));
            }
        });
        return AsyncResult.create(result, 30 * 1000);
    }
}
```
## Async 和 Logical
#### 筛选等于5
``` java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
List<AsyncMono<Integer>> collect = list.stream().map(e -> new AsyncMono<>(e)).collect(Collectors.toList());
Integer ret = Logical.create(collect).selectOne(new Logical.SelectOneCall<Integer, Integer>() {
    @Override
    public void onCall(boolean hasError, Throwable throwable, Integer result, Logical.Operation<Integer, Integer> operation) {
        if (hasError) {
            operation.doNext();
            return;
        }
        if (5 != result) {
            operation.doNext();
            return;
        }
        operation.success(result);
    }
}).toFuture().get();
```
#### 筛选大于3
``` java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
List<AsyncMono<Integer>> collect = list.stream().map(e -> new AsyncMono<>(e)).collect(Collectors.toList());
List<Integer> ret = Logical.create(collect).selectOne(new Logical.SelectOneCall<Integer, List<Integer>>() {

    private List<Integer> list = new ArrayList<>();

    @Override
    public void onCall(boolean hasError, Throwable throwable, Integer result, Logical.Operation<Integer, List<Integer>> operation) {
        if (hasError) {
            operation.doNext();
            return;
        }
        if (result > 3) {
            list.add(result);
        }
        operation.doNext();
    }

    @Override
    public List<Integer> onFinally() {
        return list;
    }
}).toFuture().get();
```
#### 数据合并
``` java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
List<AsyncMono<Integer>> collect = list.stream().map(e -> new AsyncMono<>(e)).collect(Collectors.toList());
Integer sum = Logical.create(collect).collect()
        .then(new ReactAsyncCall<List<Integer>, Integer>() {
            @Override
            public void process(boolean hasError, Throwable throwable, List<Integer> result, ReactSink<? super Integer> sink) throws Throwable {
                if (hasError) {
                    sink.finish();
                    return;
                }
                Integer sum = result.stream().collect(Collectors.summingInt(Integer::new));
                sink.success(sum);
            }
        }).toFuture().get();
```