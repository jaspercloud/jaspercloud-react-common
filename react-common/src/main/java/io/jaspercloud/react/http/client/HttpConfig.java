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
    private long writeTimeout = 10 * 1000;
    private long readTimeout = 10 * 1000;
    private int poolSize = 30;
    /**
     * ssl
     */
    private SslContext sslContext = DefaultSslContext;
    private long handshakeTimeoutMillis = 10 * 1000;
    private long closeNotifyFlushTimeoutMillis = 3 * 1000;
    private long closeNotifyReadTimeoutMillis = 0;

    /**
     * loadbalancer
     */
    private long discoveryTimeout = 10 * 1000;

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

    public HttpConfig setLoopThread(int loopThread) {
        this.loopThread = loopThread;
        return this;
    }

    public String getThreadName() {
        return threadName;
    }

    public HttpConfig setThreadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public HttpConfig setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public HttpConfig setMaxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
        return this;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public HttpConfig setMaxHeaderSize(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
        return this;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public HttpConfig setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public HttpConfig setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return this;
    }

    public boolean isFailOnMissingResponse() {
        return failOnMissingResponse;
    }

    public HttpConfig setFailOnMissingResponse(boolean failOnMissingResponse) {
        this.failOnMissingResponse = failOnMissingResponse;
        return this;
    }

    public boolean isValidateHeaders() {
        return validateHeaders;
    }

    public HttpConfig setValidateHeaders(boolean validateHeaders) {
        this.validateHeaders = validateHeaders;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public HttpConfig setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public HttpConfig setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public HttpConfig setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public HttpConfig setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public HttpConfig setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public long getHandshakeTimeoutMillis() {
        return handshakeTimeoutMillis;
    }

    public HttpConfig setHandshakeTimeoutMillis(long handshakeTimeoutMillis) {
        this.handshakeTimeoutMillis = handshakeTimeoutMillis;
        return this;
    }

    public long getCloseNotifyFlushTimeoutMillis() {
        return closeNotifyFlushTimeoutMillis;
    }

    public HttpConfig setCloseNotifyFlushTimeoutMillis(long closeNotifyFlushTimeoutMillis) {
        this.closeNotifyFlushTimeoutMillis = closeNotifyFlushTimeoutMillis;
        return this;
    }

    public long getCloseNotifyReadTimeoutMillis() {
        return closeNotifyReadTimeoutMillis;
    }

    public HttpConfig setCloseNotifyReadTimeoutMillis(long closeNotifyReadTimeoutMillis) {
        this.closeNotifyReadTimeoutMillis = closeNotifyReadTimeoutMillis;
        return this;
    }

    public long getDiscoveryTimeout() {
        return discoveryTimeout;
    }

    public HttpConfig setDiscoveryTimeout(long discoveryTimeout) {
        this.discoveryTimeout = discoveryTimeout;
        return this;
    }
}
