package io.jaspercloud.react.http.client;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

public class HttpConfig {

    private static final SslContext DefaultSslContext;

    private int loopThread = Runtime.getRuntime().availableProcessors();
    private String threadName = "netty-react-http-";
    private String userAgent = "netty-react-http";
    /**
     * http
     */
    private int maxInitialLineLength = 4096;
    private int maxHeaderSize = 8192;
    private int maxChunkSize = 8192;
    private int maxContentLength = 4 * 1024 * 1024;
    private boolean failOnMissingResponse = false;
    private boolean validateHeaders = true;
    private int connectionTimeout = 10 * 1000;
    private long readTimeout = 10 * 1000;
    private int poolSize = 30;
    /**
     * ssl
     */
    private SslContext sslContext = DefaultSslContext;
    private long handshakeTimeoutMillis = 10 * 1000;
    private long closeNotifyFlushTimeoutMillis = 3 * 1000;
    private long closeNotifyReadTimeoutMillis = 0;

    static {
        try {
            DefaultSslContext = SslContextBuilder.forClient()
                    .sslProvider(SslProvider.JDK)
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public HttpConfig() {
    }

    public int getLoopThread() {
        return loopThread;
    }

    public void setLoopThread(int loopThread) {
        this.loopThread = loopThread;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public void setMaxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public void setMaxHeaderSize(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public boolean isFailOnMissingResponse() {
        return failOnMissingResponse;
    }

    public void setFailOnMissingResponse(boolean failOnMissingResponse) {
        this.failOnMissingResponse = failOnMissingResponse;
    }

    public boolean isValidateHeaders() {
        return validateHeaders;
    }

    public void setValidateHeaders(boolean validateHeaders) {
        this.validateHeaders = validateHeaders;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public long getHandshakeTimeoutMillis() {
        return handshakeTimeoutMillis;
    }

    public void setHandshakeTimeoutMillis(long handshakeTimeoutMillis) {
        this.handshakeTimeoutMillis = handshakeTimeoutMillis;
    }

    public long getCloseNotifyFlushTimeoutMillis() {
        return closeNotifyFlushTimeoutMillis;
    }

    public void setCloseNotifyFlushTimeoutMillis(long closeNotifyFlushTimeoutMillis) {
        this.closeNotifyFlushTimeoutMillis = closeNotifyFlushTimeoutMillis;
    }

    public long getCloseNotifyReadTimeoutMillis() {
        return closeNotifyReadTimeoutMillis;
    }

    public void setCloseNotifyReadTimeoutMillis(long closeNotifyReadTimeoutMillis) {
        this.closeNotifyReadTimeoutMillis = closeNotifyReadTimeoutMillis;
    }
}
