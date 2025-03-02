package cool.scx.socket;


/**
 * RequestOptions
 *
 * @author scx567888
 * @version 0.0.1
 */
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
