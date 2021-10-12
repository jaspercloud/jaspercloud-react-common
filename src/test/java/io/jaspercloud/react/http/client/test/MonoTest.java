package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactInvokeChain;
import io.jaspercloud.react.mono.ReactSink;
import io.jaspercloud.react.mono.ReactSyncCall;
import io.jaspercloud.react.mono.invoke.InvokeRequest;
import io.jaspercloud.react.mono.invoke.InvokeResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MonoTest {

    @Test
    public void test123() throws Exception {
//        Mono<String> mono = Mono.create(new Consumer<MonoSink<String>>() {
//            @Override
//            public void accept(MonoSink<String> monoSink) {
//                monoSink.success();
//            }
//        });
//        Mono<String> result = Mono.create(new Consumer<MonoSink<String>>() {
//            @Override
//            public void accept(MonoSink<String> monoSink) {
//                mono.subscribe(new CoreSubscriber<String>() {
//                    @Override
//                    public Context currentContext() {
//                        return monoSink.currentContext();
//                    }
//
//                    @Override
//                    public void onSubscribe(Subscription s) {
//                        monoSink.onRequest(e -> {
//                            s.request(e);
//                        });
//                    }
//
//                    @Override
//                    public void onNext(String s) {
//                        monoSink.success(s);
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        monoSink.error(throwable);
//                    }
//
//                    @Override
//                    public void onsuccess() {
//                        monoSink.success();
//                    }
//                });
//            }
//        });
//        result.subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//                System.out.println();
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) {
//                System.out.println();
//            }
//        }, new Runnable() {
//            @Override
//            public void run() {
//                System.out.println();
//            }
//        });


        new AsyncMono<String>().then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.success(null);
            }
        }).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.success(null);
            }
        }).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.success("test");
            }
        }).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.success(result);
            }
        }).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.error(new RuntimeException());
                sink.success("test");
            }
        }).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.success(result);
            }
        }).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.success(result);
            }
        }).subscribe();


//        Mono.create(new Consumer<MonoSink<String>>() {
//            @Override
//            public void accept(MonoSink<String> monoSink) {
//                monoSink.success();
//            }
//        }).switchIfEmpty(Mono.create(new Consumer<MonoSink<String>>() {
//            @Override
//            public void accept(MonoSink<String> monoSink) {
//                monoSink.success("test");
//            }
//        })).handle(new BiConsumer<String, SynchronousSink<Integer>>() {
//            @Override
//            public void accept(String s, SynchronousSink<Integer> synchronousSink) {
//                synchronousSink.next(1);
//                synchronousSink.success();
//            }
//        }).handle(new BiConsumer<Integer, SynchronousSink<Integer>>() {
//            @Override
//            public void accept(Integer s, SynchronousSink<Integer> synchronousSink) {
//                synchronousSink.success();
//            }
//        }).handle(new BiConsumer<Integer, SynchronousSink<Integer>>() {
//            @Override
//            public void accept(Integer s, SynchronousSink<Integer> synchronousSink) {
//                synchronousSink.success();
//            }
//        }).switchIfEmpty(Mono.create(new Consumer<MonoSink<Integer>>() {
//            @Override
//            public void accept(MonoSink<Integer> monoSink) {
//                monoSink.success(2);
//            }
//        })).handle(new BiConsumer<Integer, SynchronousSink<Integer>>() {
//            @Override
//            public void accept(Integer s, SynchronousSink<Integer> synchronousSink) {
//                synchronousSink.success();
//            }
//        }).subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void test() throws Exception {
        Mono.just("test")
                .publishOn(Schedulers.fromExecutor(new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, "test1", 0);
                        return t;
                    }
                })))
                .map(e -> {
                    Thread thread = Thread.currentThread();
                    System.out.println(thread);
                    return e;
                })
                .publishOn(Schedulers.fromExecutor(new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, "test2", 0);
                        return t;
                    }
                })))
                .map(e -> {
                    Thread thread = Thread.currentThread();
                    System.out.println(thread);
                    return e;
                })
                .publishOn(Schedulers.fromExecutor(new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, "test3", 0);
                        return t;
                    }
                })))
                .map(e -> {
                    Thread thread = Thread.currentThread();
                    System.out.println(thread);
                    return e;
                })
                .publishOn(Schedulers.fromExecutor(new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, "test4", 0);
                        return t;
                    }
                })))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Thread thread = Thread.currentThread();
                        System.out.println(thread);
                    }
                });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void filterCall() throws Exception {
        InvokeRequest<String, String> m1 = new InvokeRequest<>("1", new AsyncMono<>(Mono.create(new Consumer<MonoSink<String>>() {
            @Override
            public void accept(MonoSink<String> monoSink) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                monoSink.success("test");
            }
        })));
        InvokeRequest<String, String> m2 = new InvokeRequest<>("2", new AsyncMono<>(Mono.create(new Consumer<MonoSink<String>>() {
            @Override
            public void accept(MonoSink<String> monoSink) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                monoSink.error(new RuntimeException());
            }
        })));
        InvokeRequest<String, String> m3 = new InvokeRequest<>("3", new AsyncMono<>(Mono.create(new Consumer<MonoSink<String>>() {
            @Override
            public void accept(MonoSink<String> monoSink) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                monoSink.success();
            }
        })));
        List<InvokeRequest<String, String>> list = Arrays.asList(m2, m3, m1);
        AsyncMono<String> asyncMono = ReactInvokeChain.filterCall(list, new ReactInvokeChain.FilterCallback<String, String, String>() {
            @Override
            public ResponseEntity<String> process(String key, boolean hasError, Throwable e, String in) {
                if (hasError) {
                    return ResponseEntity.notFound().build();
                }
                if (StringUtils.isEmpty(in)) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(in);
            }

            @Override
            public String notFound() {
                return "not found";
            }
        });
        Mono<String> mono = asyncMono.timeout(5 * 1000).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                if (hasError) {
                    sink.error(throwable);
                } else {
                    sink.success(result);
                }
            }
        }).toMono();
        mono.doOnSuccess(e -> {
            System.out.println();
        }).doOnError(e -> {
            System.out.println();
        }).doFinally(e -> {
            System.out.println();
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                System.out.println();
            }
        }, new Runnable() {
            @Override
            public void run() {
                System.out.println();
            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void mergeCall() throws Exception {
        List<InvokeRequest<String, String>> list = Arrays.asList(
                new InvokeRequest<>("1", new AsyncMono<>(Mono.just("1"))),
                new InvokeRequest<>("2", new AsyncMono<>(Mono.error(new RuntimeException()))),
                new InvokeRequest<>("3", new AsyncMono<>(Mono.just("2"))),
                new InvokeRequest<>("4", new AsyncMono<>(Mono.error(new RuntimeException()))),
                new InvokeRequest<>("5", new AsyncMono<>(Mono.just("3"))),
                new InvokeRequest<>("6", new AsyncMono<>(Mono.error(new RuntimeException()))),
                new InvokeRequest<>("7", new AsyncMono<>(Mono.create(new Consumer<MonoSink<String>>() {
                    @Override
                    public void accept(MonoSink<String> monoSink) {
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        monoSink.success("7");
                    }
                })))
        );
        AsyncMono<String> asyncMono = ReactInvokeChain.mergeCall(list, new ReactInvokeChain.MergeCallback<String, String, String>() {
            @Override
            public String process(List<InvokeResponse<String, String>> list) {
                List<String> collect = list.stream().map(e -> {
                    return e.getKey() + "->" + e.getResult();
                }).collect(Collectors.toList());
                return StringUtils.join(collect, "\r\n");
            }
        });
        asyncMono.timeout(3 * 1000).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                if (hasError) {
                    sink.error(throwable);
                } else {
                    sink.success(result);
                }
            }
        }).toMono()
                .doOnSuccess(e -> {
                    System.out.println();
                })
                .doOnError(e -> {
                    System.out.println();
                })
                .subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void contextTest() throws Exception {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
        threadLocal.set("test");
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> {
            new AsyncMono<>(Mono.create(new Consumer<MonoSink<String>>() {
                @Override
                public void accept(MonoSink<String> monoSink) {
                    String ret = threadLocal.get();
                    System.out.println(Thread.currentThread());
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    monoSink.success("test");
                }
            })).subscribe();
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void timeoutAsyncMonoTest() throws Exception {
        Mono<String> mono = Mono.create(new Consumer<MonoSink<String>>() {
            @Override
            public void accept(MonoSink<String> monoSink) {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                monoSink.success("test");
            }
        });
        new AsyncMono<>(mono).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                sink.success(result);
            }
        }).timeout(10 * 1000).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                System.out.println();
            }
        }).subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void timeoutProcessorTest() throws Exception {
        Mono<String> mono = Mono.create(new Consumer<MonoSink<String>>() {
            @Override
            public void accept(MonoSink<String> monoSink) {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                monoSink.success("test");
            }
        });
        List<InvokeRequest<String, String>> list = Arrays.asList(new InvokeRequest<>("test", new AsyncMono<>(mono)));
        AsyncMono<String> asyncMono = ReactInvokeChain.filterCall(list, new ReactInvokeChain.FilterCallback<String, String, String>() {
            @Override
            public ResponseEntity<String> process(String key, boolean hasError, Throwable throwable, String result) {
                return ResponseEntity.ok().build();
            }
        });
        asyncMono.timeout(1 * 1000).toMono().doOnSuccess(e -> {
            System.out.println();
        }).doOnError(e -> {
            System.out.println();
        }).doFinally(e -> {
            System.out.println();
        }).subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void emptyTest() throws Exception {
        new AsyncMono<String>().then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                if (hasError) {
                    sink.error(throwable);
                    return;
                }
                if (null == result) {
                    sink.success("empty data");
                } else {
                    sink.success(result);
                }
            }
        }).then(new ReactAsyncCall<String, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super String> sink) throws Throwable {
                System.out.println();
            }
        }).subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    @Test
    public void monoChainTest() {
        new AsyncMono<String>().then(new ReactAsyncCall<String, Integer>() {
            @Override
            public void process(boolean hasError, Throwable throwable, String result, ReactSink<? super Integer> sink) throws Throwable {
                sink.success(1);
            }
        }).then(new ReactAsyncCall<Integer, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, Integer result, ReactSink<? super String> sink) throws Throwable {
                sink.success("test");
            }
        }).then(new ReactSyncCall<String, Short>() {
            @Override
            protected Short process(boolean hasError, Throwable throwable, String result) throws Throwable {
                return 1;
            }
        }).then(new ReactAsyncCall<Short, String>() {
            @Override
            public void process(boolean hasError, Throwable throwable, Short result, ReactSink<? super String> sink) throws Throwable {
                sink.success("test");
            }
        }).toMono().subscribe();
    }
}
