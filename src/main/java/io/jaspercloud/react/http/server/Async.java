package io.jaspercloud.react.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * SpringMVC 异步处理
 */
public class Async {

    private static Logger logger = LoggerFactory.getLogger(Async.class);

    public static void start(HttpServletRequest request, HttpServletResponse response,
                             AsyncCall call) {
        AsyncContext async = request.startAsync(request, response);
        async.start(new Runnable() {
            @Override
            public void run() {
                call.onCall(new AsyncSession(request, response, async));
            }
        });
    }

    public static <T> DeferredResult<T> run(AsyncCallable<T> call) throws Exception {
        DeferredResult<T> result = run(null, call);
        return result;
    }

    /**
     * @param timeout 超时时间
     * @param call
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> DeferredResult<T> run(Long timeout, AsyncCallable<T> call) throws Exception {
        DeferredResult<T> result = new DeferredResult<>(timeout);
        result.onTimeout(new Runnable() {
            @Override
            public void run() {
                call.onTimeout();
            }
        });
        try {
            Mono<T> mono = call.call();
            mono.subscribe(res -> {
                result.setResult(res);
            }, err -> {
                T res = call.onThrowable(err);
                result.setResult(res);
            });
        } catch (Throwable e) {
            T res = call.onThrowable(e);
            result.setResult(res);
        }
        return result;
    }

    public interface AsyncCall {

        void onCall(AsyncSession session);
    }

    public static class AsyncSession {

        private HttpServletRequest request;
        private HttpServletResponse response;
        private AsyncContext async;

        public HttpServletRequest getRequest() {
            return request;
        }

        public HttpServletResponse getResponse() {
            return response;
        }

        public AsyncContext getAsync() {
            return async;
        }

        public AsyncSession(HttpServletRequest request, HttpServletResponse response, AsyncContext async) {
            this.request = request;
            this.response = response;
            this.async = async;
        }

        public void complete(ResponseEntity<byte[]> entity) {
            writeResponse(entity, response);
            async.complete();
        }

        private void writeResponse(ResponseEntity<byte[]> entity, HttpServletResponse response) {
            //code
            int code = entity.getStatusCodeValue();
            response.setStatus(code);
            //header
            HttpHeaders headers = entity.getHeaders();
            Iterator<Map.Entry<String, List<String>>> iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<String>> next = iterator.next();
                String key = next.getKey();
                next.getValue().forEach(val -> {
                    response.addHeader(key, val);
                });
            }
            //data
            byte[] data = entity.getBody();
            if (null == data) {
                return;
            }
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static abstract class AsyncCallable<T> {

        protected abstract Mono<T> call();

        protected abstract T onThrowable(Throwable e);

        protected void onTimeout() {

        }
    }
}
