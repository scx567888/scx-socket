package cool.scx.socket;


/**
 * PingPongOptions
 *
 * @author scx567888
 * @version 0.0.1
 */
public class PingPongOptions extends ScxSocketOptions {

    private int pingInterval;
    private int pingTimeout;

    public PingPongOptions() {
        this.pingInterval = 1000 * 10; // 心跳 间隔 10 秒
        this.pingTimeout = 1000 * 10; // 心跳 超时 10 秒
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
