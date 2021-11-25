package io.jaspercloud.react.converter;

import io.jaspercloud.react.ReactHttpInputMessage;
import io.jaspercloud.react.RequestInterceptor;
import io.jaspercloud.react.template.ReturnTemplate;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class ResponseEntityConvertInterceptor implements RequestInterceptor {

    @Autowired
    private HttpMessageConverters httpMessageConverters;

    @Override
    public Object decodeResponse(Response response, ReactHttpInputMessage inputMessage, ReturnTemplate returnTemplate) {
        if (ResponseEntity.class.isAssignableFrom(returnTemplate.getRawType())) {
            org.springframework.http.MediaType mediaType = Optional.ofNullable(response.body().contentType())
                    .map(e -> org.springframework.http.MediaType.parseMediaType(e.toString()))
                    .orElse(null);
            boolean hasMore = true;
            ParameterizedType parameterizedType = (ParameterizedType) returnTemplate.getGenericReturnType();
            do {
                Type type = parameterizedType.getActualTypeArguments()[0];
                if (type instanceof ParameterizedType) {
                    parameterizedType = (ParameterizedType) type;
                    continue;
                }
                hasMore = false;
                Class<?> rawType = (Class<?>) type;
                for (HttpMessageConverter converter : httpMessageConverters.getConverters()) {
                    if (converter.canRead(rawType, mediaType)) {
                        try {
                            Object result = converter.read(rawType, inputMessage);
                            ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.code());
                            response.headers().toMultimap().forEach((k, l) -> {
                                l.forEach(v -> {
                                    builder.header(k, v);
                                });
                            });
                            ResponseEntity<Object> entity = builder.body(result);
                            return entity;
                        } catch (IOException e) {
                        }
                    }
                }
            } while (hasMore);
        }
        return null;
    }
}
