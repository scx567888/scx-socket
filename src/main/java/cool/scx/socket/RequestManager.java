package cool.scx.socket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

final class RequestManager {

    final ConcurrentMap<Long, RequestTask> responseTaskMap;

    public RequestManager() {
        this.responseTaskMap = new ConcurrentHashMap<>();
    }

    public void setResponseCallback(ScxSocketFrame socketFrame, Consumer<ScxSocketResponse> responseCallback, RequestOptions options) {
        var requestTask = new RequestTask(responseCallback, this, options, socketFrame.seq_id);
        this.responseTaskMap.put(socketFrame.seq_id, requestTask);
        requestTask.start();
    }

    public void success(ScxSocketFrame socketFrame) {
        var requestTask = this.responseTaskMap.get(socketFrame.ack_id);
        if (requestTask != null) {
            requestTask.success(socketFrame.payload);
        }
    }

}
