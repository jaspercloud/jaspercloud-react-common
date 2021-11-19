package io.jaspercloud.react;

import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.template.ReturnTemplate;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Iterator;
import java.util.List;

public class RequestInterceptorAdapter {

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

    public Request onRequest(Request request) {
        Iterator<RequestInterceptor> iterator = interceptorList.iterator();
        RequestInterceptor.Chain<Request> chain = new RequestInterceptor.Chain<Request>() {
            @Override
            public Request proceed(Request result) {
                if (iterator.hasNext()) {
                    RequestInterceptor interceptor = iterator.next();
                    return interceptor.onRequest(result, this);
                }
                return result;
            }
        };
        Request proceed = chain.proceed(request);
        return proceed;
    }

    public Response onResponse(Response response) {
        Iterator<RequestInterceptor> iterator = interceptorList.iterator();
        RequestInterceptor.Chain<Response> chain = new RequestInterceptor.Chain<Response>() {
            @Override
            public Response proceed(Response result) {
                if (iterator.hasNext()) {
                    RequestInterceptor interceptor = iterator.next();
                    return interceptor.onResponse(result, this);
                }
                return result;
            }
        };
        Response proceed = chain.proceed(response);
        return proceed;
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
