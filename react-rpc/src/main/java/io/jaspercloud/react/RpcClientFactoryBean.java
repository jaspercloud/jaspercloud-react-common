package io.jaspercloud.react;

import io.jaspercloud.react.annotation.AnnotationProcessor;
import io.jaspercloud.react.http.client.ReactHttpClient;
import io.jaspercloud.react.template.RequestTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpcClientFactoryBean implements InitializingBean, ApplicationContextAware, BeanFactoryAware, FactoryBean<Object> {

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;

    private Class<?> type;
    private String name;
    private String url;
    private String contextId;
    private String path;
    private boolean decode404;
    private Class<?> fallback = void.class;
    private Class<?> fallbackFactory = void.class;

    private Object proxyInstance;

    @Autowired
    private ReactHttpClient reactHttpClient;

    @Autowired
    private ObjectProvider<List<AnnotationProcessor>> processorProvider;

    @Autowired
    private HttpMessageConverters httpMessageConverters;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<Method, MethodHandler> handlerMap = new HashMap<>();
        List<AnnotationProcessor> processorList = processorProvider.getIfAvailable();
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(type);
        if (null != processorList) {
            for (Method method : methods) {
                RequestTemplate requestTemplate = new RequestTemplate();
                requestTemplate.setHttpMessageConverters(httpMessageConverters);
                for (AnnotationProcessor processor : processorList) {
                    processor.process(type, method, requestTemplate);
                }
                handlerMap.put(method, new MethodHandler(reactHttpClient, requestTemplate));
            }
        }
        proxyInstance = Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("equals".equals(method.getName())) {
                    try {
                        Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                        return equals(otherHandler);
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                } else if ("hashCode".equals(method.getName())) {
                    return hashCode();
                } else if ("toString".equals(method.getName())) {
                    return toString();
                }
                return handlerMap.get(method).invoke(args);
            }
        });
    }

    @Override
    public Object getObject() throws Exception {
        return proxyInstance;
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public RpcClientFactoryBean(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDecode404() {
        return decode404;
    }

    public void setDecode404(boolean decode404) {
        this.decode404 = decode404;
    }

    public Class<?> getFallback() {
        return fallback;
    }

    public void setFallback(Class<?> fallback) {
        this.fallback = fallback;
    }

    public Class<?> getFallbackFactory() {
        return fallbackFactory;
    }

    public void setFallbackFactory(Class<?> fallbackFactory) {
        this.fallbackFactory = fallbackFactory;
    }
}
