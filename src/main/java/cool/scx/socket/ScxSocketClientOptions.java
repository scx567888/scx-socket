package cool.scx.socket;

public class ScxSocketClientOptions extends ScxSocketOptions {

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
