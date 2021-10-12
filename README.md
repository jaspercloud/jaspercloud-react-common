# jaspercloud-react-common
## react-http-client
``` java
ReactHttpClient reactHttpClient = new ReactHttpClient(1);
reactHttpClient.execute(new Request.Builder()
.url("http://www.baidu.com")
.build())
.timeout(50000)
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
