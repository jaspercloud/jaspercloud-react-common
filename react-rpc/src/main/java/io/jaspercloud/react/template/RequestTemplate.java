package io.jaspercloud.react.template;

import io.jaspercloud.exception.ReactRpcException;
import io.jaspercloud.react.ReactHttpInputMessage;
import io.jaspercloud.react.ReactHttpOutputMessage;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RequestTemplate {

    private String method;
    private String baseUrl;
    private UriTemplate uriTemplate;
    private List<ParameterTemplate> pathVariables = new ArrayList<>();
    private List<ParameterTemplate> params = new ArrayList<>();
    private List<ParameterTemplate> headers = new ArrayList<>();
    private Integer bodyIndex;
    private ReturnTemplate returnTemplate;
    private HttpMessageConverters httpMessageConverters;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(UriTemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public List<ParameterTemplate> getPathVariables() {
        return pathVariables;
    }

    public void setPathVariables(List<ParameterTemplate> pathVariables) {
        this.pathVariables = pathVariables;
    }

    public List<ParameterTemplate> getParams() {
        return params;
    }

    public void setParams(List<ParameterTemplate> params) {
        this.params = params;
    }

    public List<ParameterTemplate> getHeaders() {
        return headers;
    }

    public void setHeaders(List<ParameterTemplate> headers) {
        this.headers = headers;
    }

    public Integer getBodyIndex() {
        return bodyIndex;
    }

    public void setBodyIndex(Integer bodyIndex) {
        this.bodyIndex = bodyIndex;
    }

    public ReturnTemplate getReturnTemplate() {
        return returnTemplate;
    }

    public void setReturnTemplate(ReturnTemplate returnTemplate) {
        this.returnTemplate = returnTemplate;
    }

    public HttpMessageConverters getHttpMessageConverters() {
        return httpMessageConverters;
    }

    public void setHttpMessageConverters(HttpMessageConverters httpMessageConverters) {
        this.httpMessageConverters = httpMessageConverters;
    }

    public Request buildRequest(Object[] args) {
        Map<String, Object> pathVariableMap = parseParamMap(pathVariables, args);
        MultiValueMap<String, Object> paramMap = parseParamMultiValueMap(params, args);
        MultiValueMap<String, Object> headerMap = parseParamMultiValueMap(headers, args);
        Request.Builder builder = new Request.Builder();
        builder.url(parseUrl(pathVariableMap, paramMap));
        headerMap.forEach((k, l) -> {
            l.forEach(v -> {
                builder.header(k, Optional.ofNullable(v)
                        .map(e -> e.toString())
                        .orElse(null));
            });
        });
        if (null != bodyIndex) {
            Object body = args[bodyIndex];
            builder.method(method, convertRequestBody(body));
        } else {
            builder.method(method, null);
        }
        return builder.build();
    }

    private RequestBody convertRequestBody(Object data) {
        List<HttpMessageConverter<?>> converters = httpMessageConverters.getConverters();
        for (HttpMessageConverter converter : converters) {
            if (converter.canWrite(data.getClass(), null)) {
                try {
                    ReactHttpOutputMessage outputMessage = new ReactHttpOutputMessage();
                    converter.write(data, null, outputMessage);
                    MediaType mediaType = Optional.ofNullable(outputMessage.getHeaders().getContentType())
                            .map(e -> MediaType.parse(e.toString()))
                            .orElse(null);
                    return RequestBody.create(mediaType, outputMessage.getBytes());
                } catch (IOException e) {
                    throw new ReactRpcException(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    public Object convertResponseBody(Response response) throws IOException {
        if (200 != response.code()) {
            throw new ReactRpcException(String.format("response.code=%s", response.code()));
        }
        org.springframework.http.MediaType mediaType = Optional.ofNullable(response.body().contentType())
                .map(e -> org.springframework.http.MediaType.parseMediaType(e.toString()))
                .orElse(null);
        byte[] bytes = response.body().bytes();
        ReactHttpInputMessage inputMessage = new ReactHttpInputMessage(new ByteArrayInputStream(bytes));
        for (HttpMessageConverter converter : httpMessageConverters.getConverters()) {
            Class<?> returnClass = returnTemplate.getReturnClass();
            Class<?> rawType = returnTemplate.getRawType();
            if (null != rawType) {
                if (converter.canRead(rawType, mediaType)) {
                    Object result = converter.read(rawType, inputMessage);
                    return result;
                }
            } else if (converter.canRead(returnClass, mediaType)) {
                Object result = converter.read(returnClass, inputMessage);
                return result;
            }
        }
        throw new UnsupportedOperationException();
    }

    private String parseUrl(Map<String, Object> pathVariableMap, MultiValueMap<String, Object> paramMap) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        List<UriTemplate.Node> nodeList = uriTemplate.getNodeList();
        for (UriTemplate.Node node : nodeList) {
            if (node instanceof UriTemplate.ExpressionNode) {
                UriTemplate.ExpressionNode expressionNode = (UriTemplate.ExpressionNode) node;
                builder.path(Optional.ofNullable(pathVariableMap.get(expressionNode.getName()))
                        .map(e -> e.toString())
                        .orElse(null));
            } else {
                UriTemplate.StringNode stringNode = (UriTemplate.StringNode) node;
                builder.path(stringNode.getTemplate());
            }
        }
        paramMap.forEach((k, l) -> {
            l.forEach(v -> {
                builder.queryParam(k, v);
            });
        });
        return builder.build().toString();
    }

    private MultiValueMap<String, Object> parseParamMultiValueMap(List<ParameterTemplate> list, Object[] args) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        for (ParameterTemplate item : list) {
            Object arg = args[item.getIndex()];
            if (item.isRequired() && null == arg) {
                throw new ReactRpcException(String.format("%s required", item.getName()));
            } else if (null == arg) {
                arg = item.getDefaultValue();
            }
            map.add(item.getName(), arg);
        }
        return map;
    }

    private Map<String, Object> parseParamMap(List<ParameterTemplate> list, Object[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (ParameterTemplate item : list) {
            Object arg = args[item.getIndex()];
            if (item.isRequired() && null == arg) {
                throw new ReactRpcException(String.format("%s required", item.getName()));
            } else if (null == arg) {
                arg = item.getDefaultValue();
            }
            map.put(item.getName(), arg);
        }
        return map;
    }
}
