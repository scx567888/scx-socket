package cool.scx.socket;

import cool.scx.socket.ping_pong.PingPongOptions;

public class ScxSocketServerOptions extends PingPongOptions {

    private int removeClosedClientTimeout;

    public ScxSocketServerOptions() {
        //默认 30 分钟
        this.removeClosedClientTimeout = 1000 * 60 * 30;
    }

    public int getRemoveClosedClientTimeout() {
        return removeClosedClientTimeout;
    }

    public ScxSocketServerOptions setRemoveClosedClientTimeout(int removeClosedClientTimeout) {
        this.removeClosedClientTimeout = removeClosedClientTimeout;
        return this;
    }

}
