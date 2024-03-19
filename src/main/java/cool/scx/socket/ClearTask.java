package cool.scx.socket;

import io.netty.util.Timeout;

import static cool.scx.socket.DuplicateFrameChecker.Key;
import static cool.scx.socket.Helper.setTimeout;

final class ClearTask {

    private final DuplicateFrameChecker checker;
    private final Key key;
    private volatile Timeout clearTimeout;

    public ClearTask(Key key, DuplicateFrameChecker checker) {
        this.key = key;
        this.checker = checker;
    }

    public synchronized void start() {
        cancel();
        this.clearTimeout = setTimeout(this::clear, this.checker.getClearTimeout());
    }

    public synchronized void cancel() {
        if (this.clearTimeout != null) {
            this.clearTimeout.cancel();
            this.clearTimeout = null;
        }
    }

    private void clear() {
        this.checker.clearTaskMap.remove(key);
    }

}
