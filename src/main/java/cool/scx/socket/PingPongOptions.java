package cool.scx.socket;

public class PingPongOptions extends ScxSocketOptions {

    private int pingInterval;
    private int pingTimeout;

    public PingPongOptions() {
        this.pingInterval = 1000 * 5; // 心跳 间隔 5 秒
        this.pingTimeout = 1000 * 5; // 心跳 超时 5 秒
    }

    public final int getPingInterval() {
        return pingInterval;
    }

    public final PingPongOptions setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
        return this;
    }

    public final int getPingTimeout() {
        return pingTimeout;
    }

    public final PingPongOptions setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
        return this;
    }

}
