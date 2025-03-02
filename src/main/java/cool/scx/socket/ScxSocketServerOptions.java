package cool.scx.socket;

import java.util.concurrent.Executor;


/**
 * ScxSocketServerOptions
 *
 * @author scx567888
 * @version 0.0.1
 */
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

    @Override
    public ScxSocketServerOptions executor(Executor executor) {
        super.executor(executor);
        return this;
    }

    @Override
    public ScxSocketServerOptions setDuplicateFrameCheckerClearTimeout(int duplicateFrameCheckerClearTimeout) {
        super.setDuplicateFrameCheckerClearTimeout(duplicateFrameCheckerClearTimeout);
        return this;
    }

}
