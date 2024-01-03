package cool.scx.socket;

import io.netty.util.Timeout;

import static cool.scx.socket.ScxSocketHelper.setTimeout;

public final class SeqIDClearTask {

    private final DuplicateFrameChecker checker;
    private final DuplicateFrameKey key;
    private Timeout clearTimeout;

    public SeqIDClearTask(DuplicateFrameKey key, DuplicateFrameChecker checker) {
        this.key = key;
        this.checker = checker;
    }

    public void start() {
        cancel();
        clearTimeout = setTimeout(this::clear, checker.getSeqIDClearTimeout());
    }

    public void cancel() {
        if (clearTimeout != null) {
            clearTimeout.cancel();
            clearTimeout = null;
        }
    }

    private void clear() {
        checker.seqIDClearTaskMap.remove(key);
    }

}
