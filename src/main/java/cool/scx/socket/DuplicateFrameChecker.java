package cool.scx.socket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 重复帧检查器
 */
public final class DuplicateFrameChecker {

    final ConcurrentMap<DuplicateFrameKey, SeqIDClearTask> seqIDClearTaskMap;

    /**
     * 重复帧校验 清理延时
     */
    final long seqIDClearTimeout;

    public DuplicateFrameChecker(long seqIDClearTimeout) {
        this.seqIDClearTaskMap = new ConcurrentHashMap<>();
        this.seqIDClearTimeout = seqIDClearTimeout;
    }

    /**
     * 用来判断是否为重发的消息
     *
     * @param socketFrame socketFrame
     * @return true 是重发 false 不是重发
     */
    public boolean checkDuplicate(ScxSocketFrame socketFrame) {
        //只要 need_ack 的都可能会重发 所以需要 做校验
        if (!socketFrame.need_ack) {
            return true;
        }
        var key = new DuplicateFrameKey(socketFrame.seq_id, socketFrame.now);
        var task = seqIDClearTaskMap.get(key);
        if (task == null) {
            var seqIDClearTask = new SeqIDClearTask(key, this);
            seqIDClearTaskMap.put(key, seqIDClearTask);
            seqIDClearTask.start();
            return true;
        } else {
            return false;
        }
    }

    public void startAllClearTask() {
        for (var value : seqIDClearTaskMap.values()) {
            value.start();
        }
    }

    public void cancelAllClearTask() {
        for (var value : seqIDClearTaskMap.values()) {
            value.cancel();
        }
    }

    public void startAllClearTaskAsync() {
        Thread.ofVirtual().start(this::startAllClearTask);
    }

    public void cancelAllClearTaskAsync() {
        Thread.ofVirtual().start(this::cancelAllClearTask);
    }

    public long getSeqIDClearTimeout() {
        return seqIDClearTimeout;
    }

}
