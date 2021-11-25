package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.RequestInterceptor;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.net.URISyntaxException;

public class LoadBalancerRequestInterceptor implements RequestInterceptor {

    private static Logger logger = LoggerFactory.getLogger(LoadBalancerRequestInterceptor.class);

    private ReactiveServiceInstanceChooser instanceChooser;

    public LoadBalancerRequestInterceptor(ReactiveServiceInstanceChooser instanceChooser) {
        this.instanceChooser = instanceChooser;
    }

    @Override
    public AsyncMono<Request> onRequest(Request request, Chain<Request> chain) {
        Request originalRequest = request;
        URI original = URI.create(originalRequest.url().toString());
        String host = original.getHost();
        //find ServiceInstance
        AsyncMono<Request> asyncMono = instanceChooser.choose(host)
                .then(new ReactAsyncCall<ServiceInstance, Request>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, ServiceInstance instance, ReactSink<? super Request> sink) throws Throwable {
                        if (hasError) {
                            chain.proceed(originalRequest).subscribe(sink);
                            return;
                        }
                        URI uri = reconstructURIWithServer(instance, original);
                        Request.Builder builder = originalRequest.newBuilder()
                                .url(uri.toString());
                        builder.header("http2", "true");
                        Request req = builder.build();
                        chain.proceed(req).subscribe(sink);
                    }
                });
        return asyncMono;
    }

    public URI reconstructURIWithServer(ServiceInstance instance, URI original) {
        String host = instance.getHost();
        int port = instance.getPort();
        if (host.equals(original.getHost())
                && port == original.getPort()) {
            return original;
        }
        String scheme = original.getScheme();
        if (scheme == null) {
            scheme = deriveScheme(original);
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(scheme).append("://");
            if (!StringUtils.isEmpty(original.getRawUserInfo())) {
                sb.append(original.getRawUserInfo()).append("@");
            }
            sb.append(host);
            if (port >= 0) {
                sb.append(":").append(port);
            }
            sb.append(original.getRawPath());
            if (!StringUtils.isEmpty(original.getRawQuery())) {
                sb.append("?").append(original.getRawQuery());
            }
            if (!StringUtils.isEmpty(original.getRawFragment())) {
                sb.append("#").append(original.getRawFragment());
            }
            URI newURI = new URI(sb.toString());
            return newURI;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String deriveScheme(URI uri) {
        boolean isSecure = false;
        String scheme = uri.getScheme();
        if (scheme != null) {
            isSecure = scheme.equalsIgnoreCase("https");
        }
        if (scheme == null) {
            if (isSecure) {
                scheme = "https";
            } else {
                scheme = "http";
            }
        }
        return scheme;
    }
}
