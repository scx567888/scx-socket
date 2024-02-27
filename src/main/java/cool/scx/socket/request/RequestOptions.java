package cool.scx.socket.request;

import cool.scx.socket.sender.SendOptions;

public class RequestOptions extends SendOptions {

    private int requestTimeout;

    public RequestOptions() {
        this.requestTimeout = 1000 * 10; // 默认请求超时 10 秒
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

}
