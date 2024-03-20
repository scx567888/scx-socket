package cool.scx.socket;

public final class RequestOptions extends SendOptions {

    private int requestTimeout;

    public RequestOptions() {
        this.requestTimeout = 1000 * 10; // 默认请求超时 10 秒
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public RequestOptions setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

}
