package cool.scx.socket;

import cool.scx.socket.ping_pong.PingPongOptions;

public class ScxSocketServerOptions extends PingPongOptions {

    private int statusKeepTime;

    public ScxSocketServerOptions() {
        //默认 30 分钟
        this.statusKeepTime = 1000 * 60 * 30;
    }

    public int getStatusKeepTime() {
        return statusKeepTime;
    }

    public ScxSocketServerOptions setStatusKeepTime(int statusKeepTime) {
        this.statusKeepTime = statusKeepTime;
        return this;
    }

}
