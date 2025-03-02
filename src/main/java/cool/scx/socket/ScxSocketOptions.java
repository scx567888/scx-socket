package cool.scx.socket;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * ScxSocketOptions
 *
 * @author scx567888
 * @version 0.0.1
 */
public class ScxSocketOptions {

    /**
     * 重复帧检查器清除超时
     */
    private int duplicateFrameCheckerClearTimeout;

    /**
     * 调度器 : 用于消息重发, 心跳检测等
     */
    private ScheduledExecutorService scheduledExecutor;

    /**
     * 执行器 : 用于执行 onMessage onEvent 之类的回调
     */
    private Executor executor;

    public ScxSocketOptions() {
        // 默认 10 分钟
        this.duplicateFrameCheckerClearTimeout = 1000 * 60 * 10;
        // 默认 cpu 核心数 2倍
        this.scheduledExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        // 默认 cpu 核心数 2倍
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public int getDuplicateFrameCheckerClearTimeout() {
        return duplicateFrameCheckerClearTimeout;
    }

    public ScxSocketOptions setDuplicateFrameCheckerClearTimeout(int duplicateFrameCheckerClearTimeout) {
        this.duplicateFrameCheckerClearTimeout = duplicateFrameCheckerClearTimeout;
        return this;
    }

    public Executor executor() {
        return executor;
    }

    public ScheduledExecutorService scheduledExecutor() {
        return scheduledExecutor;
    }

    public ScxSocketOptions executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public ScxSocketOptions scheduledExecutor(ScheduledExecutorService scheduledExecutor) {
        this.scheduledExecutor = scheduledExecutor;
        return this;
    }

}
