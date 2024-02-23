package cool.scx.socket_new;

public class ScxSocketOptions {

    private int pingInterval;

    private int pingTimeout;

    private int seqIDClearTimeout;

    public ScxSocketOptions() {
        this.pingInterval = 1000 * 5;
        this.pingTimeout = 1000 * 5;
        this.seqIDClearTimeout = 1000 * 60 * 10;
    }

    public final int getPingInterval() {
        return pingInterval;
    }

    public final ScxSocketOptions setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
        return this;
    }

    public final int getPingTimeout() {
        return pingTimeout;
    }

    public final ScxSocketOptions setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
        return this;
    }

    public final int getSeqIDClearTimeout() {
        return seqIDClearTimeout;
    }

    public final ScxSocketOptions setSeqIDClearTimeout(int seqIDClearTimeout) {
        this.seqIDClearTimeout = seqIDClearTimeout;
        return this;
    }

}
