package io.jaspercloud.react.http.client.test;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactSyncCall;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class MonoChainTest {

    @Test
    public void test() throws Exception {
        AsyncMono<String> asyncMono = new AsyncMono<>();
        for (int i = 1; i <= 3; i++) {
            int n = i;
            asyncMono = asyncMono.then(new ReactSyncCall<String, String>() {

                @Override
                public void onSubscribe() {
                    System.out.println("onSubscribe " + n);
                }

                @Override
                public void onFinally() {
                    System.out.println("onFinally " + n);
                }

                @Override
                protected String process(boolean hasError, Throwable throwable, String result) throws Throwable {
                    System.out.println("process " + n);
                    if (hasError) {
                        throw throwable;
                    }
                    return result;
                }
            });
        }
        asyncMono.subscribe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
