package cool.scx.socket;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static cool.scx.socket.DuplicateFrameChecker.Key;

/**
 * ClearTask
 *
 * @author scx567888
 * @version 0.0.1
 */
final class ClearTask {

    private final DuplicateFrameChecker checker;
    private final Key key;
    private final ScheduledExecutorService scheduledExecutor;
    private final Lock lock = new ReentrantLock();
    private ScheduledFuture<?> clearTimeout;

    public ClearTask(Key key, DuplicateFrameChecker checker) {
        this.key = key;
        this.checker = checker;
        this.scheduledExecutor = checker.scheduledExecutor;
    }

    public void start() {
        lock.lock();
        try {
            cancel();
            this.clearTimeout = scheduledExecutor.schedule(this::clear, this.checker.getClearTimeout(), TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void cancel() {
        lock.lock();
        try {
            if (this.clearTimeout != null) {
                this.clearTimeout.cancel(false);
                this.clearTimeout = null;
            }
        } finally {
            lock.unlock();
        }
    }

    private void clear() {
        this.checker.clearTaskMap.remove(key);
    }

}
