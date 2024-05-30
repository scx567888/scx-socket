package cool.scx.socket;

public final class ScxSocketClientOptions extends PingPongOptions {

    private int reconnectTimeout;

    public ScxSocketClientOptions() {
        this.reconnectTimeout = 1000 * 5;
    }

    public int getReconnectTimeout() {
        return reconnectTimeout;
    }

    public ScxSocketClientOptions setReconnectTimeout(int reconnectTimeout) {
        this.reconnectTimeout = reconnectTimeout;
        return this;
    }

}
