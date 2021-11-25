package io.jaspercloud.react;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.template.ReturnTemplate;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class RequestInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(RequestInterceptorAdapter.class);
    private List<RequestInterceptor> interceptorList;

    public RequestInterceptorAdapter(List<RequestInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    public String convertParam(String name, Object value) {
        for (RequestInterceptor interceptor : interceptorList) {
            String result = interceptor.convertParam(name, value);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    public AsyncMono<Request> onRequest(Request request) {
        Iterator<RequestInterceptor> iterator = interceptorList.iterator();
        RequestInterceptor.Chain<Request> chain = new RequestInterceptor.Chain<Request>() {
            @Override
            public AsyncMono<Request> proceed(Request result) {
                boolean error;
                do {
                    try {
                        if (iterator.hasNext()) {
                            RequestInterceptor interceptor = iterator.next();
                            return interceptor.onRequest(result, this);
                        }
                        return new AsyncMono<>(result);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        error = true;
                    }
                } while (error);
                return new AsyncMono<>(result);
            }
        };
        AsyncMono<Request> proceed = chain.proceed(request);
        return proceed;
    }

    public AsyncMono<Response> onResponse(Response response) {
        Iterator<RequestInterceptor> iterator = interceptorList.iterator();
        RequestInterceptor.Chain<Response> chain = new RequestInterceptor.Chain<Response>() {
            @Override
            public AsyncMono<Response> proceed(Response result) {
                boolean error;
                do {
                    try {
                        if (iterator.hasNext()) {
                            RequestInterceptor interceptor = iterator.next();
                            return interceptor.onResponse(result, this);
                        }
                        return new AsyncMono<>(result);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        error = true;
                    }
                } while (error);
                return new AsyncMono<>(result);
            }
        };
        AsyncMono<Response> proceed = chain.proceed(response);
        return proceed;
    }

    public Object decodeResponse(Response response, ReactHttpInputMessage inputMessage, ReturnTemplate returnTemplate) {
        for (RequestInterceptor interceptor : interceptorList) {
            Object result = interceptor.decodeResponse(response, inputMessage, returnTemplate);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    public Object onReturn(ReturnTemplate returnTemplate, AsyncMono<Object> asyncMono) {
        for (RequestInterceptor interceptor : interceptorList) {
            Object result = interceptor.onReturn(returnTemplate, asyncMono);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

}
