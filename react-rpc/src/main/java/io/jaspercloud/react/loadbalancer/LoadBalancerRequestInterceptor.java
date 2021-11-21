package io.jaspercloud.react.loadbalancer;

import com.google.common.base.Strings;
import io.jaspercloud.react.RequestInterceptor;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import okhttp3.Request;
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
        URI original = URI.create(request.url().toString());
        String host = original.getHost();
        //find ServiceInstance
        AsyncMono<Request> asyncMono = instanceChooser.choose(host)
                .then(new ReactAsyncCall<ServiceInstance, Request>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, ServiceInstance instance, ReactSink<? super Request> sink) throws Throwable {
                        if (hasError) {
                            sink.finish();
                            return;
                        }
                        URI uri = reconstructURIWithServer(instance, original);
                        Request req = request.newBuilder().url(uri.toString()).build();
                        sink.success(req);
                    }
                })
                .then(new ReactAsyncCall<Request, Request>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, Request request, ReactSink<? super Request> sink) throws Throwable {
                        if (hasError) {
                            logger.error(throwable.getMessage(), throwable);
                            chain.proceed(originalRequest).subscribe(sink);
                            return;
                        }
                        chain.proceed(request).subscribe(sink);
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
            if (!Strings.isNullOrEmpty(original.getRawUserInfo())) {
                sb.append(original.getRawUserInfo()).append("@");
            }
            sb.append(host);
            if (port >= 0) {
                sb.append(":").append(port);
            }
            sb.append(original.getRawPath());
            if (!Strings.isNullOrEmpty(original.getRawQuery())) {
                sb.append("?").append(original.getRawQuery());
            }
            if (!Strings.isNullOrEmpty(original.getRawFragment())) {
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
