package cool.scx.socket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 重复帧检查器
 *
 * @author scx567888
 * @version 0.0.1
 */
final class DuplicateFrameChecker {

    final ConcurrentMap<Key, ClearTask> clearTaskMap;
    final ScheduledExecutorService scheduledExecutor;

    /**
     * 重复帧校验 清理延时
     */
    private final long clearTimeout;

    public DuplicateFrameChecker(ScxSocketOptions options) {
        this.clearTaskMap = new ConcurrentHashMap<>();
        this.clearTimeout = options.getDuplicateFrameCheckerClearTimeout();
        this.scheduledExecutor = options.scheduledExecutor();
    }

    /**
     * 用来判断是否为重发的消息
     *
     * @param socketFrame socketFrame
     * @return true 是不是重发 false 是重发
     */
    public boolean check(ScxSocketFrame socketFrame) {
        //如果 need_ack 则不会重复发送 直接返回
        if (!socketFrame.need_ack) {
            return true;
        }
        var key = new Key(socketFrame.seq_id, socketFrame.now);
        var notDuplicate = new AtomicBoolean();
        var task = clearTaskMap.compute(key, (k, v) -> {
            //如果旧值为空 则表示这是第一次接受到数据
            if (v == null) {
                notDuplicate.set(true);
                return new ClearTask(key, this);
            } else { //否则不改变任何数据
                notDuplicate.set(false);
                return v;
            }
        });
        var b = notDuplicate.get();
        //如果 不是重复发送则启动清除任务 如果是重复发送则清除任务可能已经启动 此处不需要重复启动
        if (b) {
            task.start();
        }
        return b;
    }

    public void startAllClearTask() {
        for (var value : clearTaskMap.values()) {
            value.start();
        }
    }

    public void cancelAllClearTask() {
        for (var value : clearTaskMap.values()) {
            value.cancel();
        }
    }

    public long getClearTimeout() {
        return clearTimeout;
    }

    record Key(long seq_id, long now) {

    }

}
