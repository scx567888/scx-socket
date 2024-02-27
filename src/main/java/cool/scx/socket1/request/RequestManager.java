package cool.scx.socket1.request;

import cool.scx.socket1.frame.ScxSocketFrame;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

public class RequestManager {

    final ConcurrentMap<Long, RequestTask> responseTaskMap;

    public RequestManager() {
        this.responseTaskMap = new ConcurrentHashMap<>();
    }

    public final void setResponseCallback(ScxSocketFrame socketFrame, BiConsumer<String, Throwable> responseCallback, RequestOptions options) {
        var requestTask = new RequestTask(responseCallback, this, options, socketFrame.seq_id);
        this.responseTaskMap.put(socketFrame.seq_id, requestTask);
        requestTask.start();
    }

    protected void success(ScxSocketFrame socketFrame) {
        var requestTask = this.responseTaskMap.get(socketFrame.ack_id);
        if (requestTask != null) {
            requestTask.success(socketFrame.payload);
        }
    }

    public void successAsync(ScxSocketFrame socketFrame) {
        var requestTask = this.responseTaskMap.get(socketFrame.ack_id);
        if (requestTask != null) {
            Thread.ofVirtual().start(() -> requestTask.success(socketFrame.payload));
        }
    }

}
