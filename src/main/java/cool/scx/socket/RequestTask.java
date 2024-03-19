package cool.scx.socket;

import io.netty.util.Timeout;

import java.util.function.BiConsumer;

public class RequestTask {

    private final BiConsumer<String, Throwable> responseCallback;
    private final RequestManager requestManager;
    private final RequestOptions options;
    private final long seq_id;
    private Timeout failTimeout;

    public RequestTask(BiConsumer<String, Throwable> responseCallback, RequestManager requestManager, RequestOptions options, long seqId) {
        this.responseCallback = responseCallback;
        this.requestManager = requestManager;
        this.options = options;
        this.seq_id = seqId;
    }

    public void start() {
        cancelFail();
        this.failTimeout = Helper.setTimeout(this::fail, options.getRequestTimeout());
    }

    public void success(String payload) {
        this.clear();
        this.responseCallback.accept(payload, null);
    }

    public void fail() {
        this.clear();
        this.responseCallback.accept(null, new RuntimeException("超时"));
    }

    public void cancelFail() {
        if (this.failTimeout != null) {
            this.failTimeout.cancel();
            this.failTimeout = null;
        }
    }

    public void clear() {
        this.cancelFail();
        this.requestManager.responseTaskMap.remove(seq_id);
    }

}
