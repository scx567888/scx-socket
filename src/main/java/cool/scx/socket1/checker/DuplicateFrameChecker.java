package cool.scx.socket1.checker;

import cool.scx.socket1.frame.ScxSocketFrame;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 重复帧检查器
 */
final class DuplicateFrameChecker {

    public final ConcurrentMap<Key, ClearTask> clearTaskMap;

    /**
     * 重复帧校验 清理延时
     */
    public final long clearTimeout;

    public DuplicateFrameChecker(long clearTimeout) {
        this.clearTaskMap = new ConcurrentHashMap<>();
        this.clearTimeout = clearTimeout;
    }

    /**
     * 用来判断是否为重发的消息
     *
     * @param socketFrame socketFrame
     * @return true 是重发 false 不是重发
     */
    public boolean check(ScxSocketFrame socketFrame) {
        //只要 need_ack 的都可能会重发 所以需要 做校验
        if (!socketFrame.need_ack) {
            return true;
        }
        var key = new Key(socketFrame.seq_id, socketFrame.now);
        var task = clearTaskMap.get(key);
        if (task == null) {
            var clearTask = new ClearTask(key, this);
            clearTaskMap.put(key, clearTask);
            clearTask.start();
            return true;
        } else {
            return false;
        }
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

    public void startAllClearTaskAsync() {
        Thread.ofVirtual().start(this::startAllClearTask);
    }

    public void cancelAllClearTaskAsync() {
        Thread.ofVirtual().start(this::cancelAllClearTask);
    }

    public long getClearTimeout() {
        return clearTimeout;
    }

}
