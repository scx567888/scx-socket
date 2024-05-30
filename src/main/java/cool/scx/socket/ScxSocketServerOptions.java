package cool.scx.socket;

public final class ScxSocketServerOptions extends PingPongOptions {

    private int statusKeepTime;

    public ScxSocketServerOptions() {
        this.statusKeepTime = 1000 * 60 * 30; // 默认 30 分钟
    }

    public int getStatusKeepTime() {
        return statusKeepTime;
    }

    public ScxSocketServerOptions setStatusKeepTime(int statusKeepTime) {
        this.statusKeepTime = statusKeepTime;
        return this;
    }

}
