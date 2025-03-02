package cool.scx.socket;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * RequestTask
 *
 * @author scx567888
 * @version 0.0.1
 */
final class RequestTask {

    private final Consumer<ScxSocketResponse> responseCallback;
    private final RequestManager requestManager;
    private final RequestOptions options;
    private final long seq_id;
    private final ScheduledExecutorService scheduledExecutor;
    private ScheduledFuture<?> failTimeout;

    public RequestTask(Consumer<ScxSocketResponse> responseCallback, RequestManager requestManager, RequestOptions options, long seqId) {
        this.responseCallback = responseCallback;
        this.requestManager = requestManager;
        this.options = options;
        this.seq_id = seqId;
        this.scheduledExecutor = requestManager.scheduledExecutor;
    }

    public void start() {
        cancelFail();
        this.failTimeout = scheduledExecutor.schedule(this::fail, options.getRequestTimeout(), TimeUnit.MILLISECONDS);
    }

    public void success(String payload) {
        this.clear();
        this.responseCallback.accept(new ScxSocketResponse(payload));
    }

    public void fail() {
        this.clear();
        this.responseCallback.accept(new ScxSocketResponse(new RuntimeException("超时")));
    }

    public void cancelFail() {
        if (this.failTimeout != null) {
            this.failTimeout.cancel(false);
            this.failTimeout = null;
        }
    }

    public void clear() {
        this.cancelFail();
        this.requestManager.responseTaskMap.remove(seq_id);
    }

}
