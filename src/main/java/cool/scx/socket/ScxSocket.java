package cool.scx.socket;

import io.vertx.core.http.WebSocketBase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static cool.scx.util.StringUtils.isBlank;
import static java.lang.System.Logger.Level.DEBUG;

public class ScxSocket extends EventManager {

    protected final ConcurrentMap<Long, SendTask> sendTaskMap;

    public ScxSocket(ScxSocketOptions options, String clientID) {
        super(options,clientID);
        this.sendTaskMap = new ConcurrentHashMap<>();
    }


    @Override
    protected void send(ScxSocketFrame socketFrame, SendOptions options) {
        var sendTask = new SendTask(socketFrame, options, this);
        this.sendTaskMap.put(socketFrame.seq_id, sendTask);
        sendTask.start();
    }

    private void startAllSendTask() {
        for (var value : sendTaskMap.values()) {
            value.start();
        }
    }

    private void cancelAllResendTask() {
        for (var value : sendTaskMap.values()) {
            value.cancelResend();
        }
    }

    private void startAllSendTaskAsync() {
        Thread.ofVirtual().start(this::startAllSendTask);
    }

    private void cancelAllResendTaskAsync() {
        Thread.ofVirtual().start(this::cancelAllResendTask);
    }

    @Override
    protected void doMessage(ScxSocketFrame socketFrame) {
        // ACK 应第一时间返回
        if (socketFrame.need_ack) {
            sendAck(socketFrame.seq_id);
        }
        if (isBlank(socketFrame.event_name)) {
            callOnMessageWithCheckDuplicateAsync(socketFrame);
        } else {
            callOnEventWithCheckDuplicateAsync(socketFrame);
        }

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "收到消息 : {0}", socketFrame.toJson());
        }

    }

    @Override
    protected void doResponse(ScxSocketFrame socketFrame) {
        // ACK 应第一时间返回
        if (socketFrame.need_ack) {
            sendAck(socketFrame.seq_id);
        }
        callResponseCallbackAsync(socketFrame);
    }

    @Override
    protected void doAck(ScxSocketFrame ackFrame) {
        var sendTask = sendTaskMap.get(ackFrame.ack_id);
        if (sendTask != null) {
            sendTask.clear();
        }
    }

    @Override
    protected void doClose(Void v) {
        this.close();
        //呼叫 onClose 事件
        this.callOnClose(v);
    }

    @Override
    protected void doError(Throwable e) {
        this.close();
        //呼叫 onClose 事件
        this.callOnError(e);
    }

    void start(WebSocketBase webSocket) {
        close();
        //绑定事件
        this.bind(webSocket);
        //启动所有发送任务
        this.startAllSendTask();
        //启动 校验重复清除任务
        this.duplicateFrameChecker.startAllClearTask();
    }

    public void close() {
        //移除绑定事件
        this.removeBind();
        //关闭 连接
        this.closeWebSocket();
        //取消所有重发任务
        this.cancelAllResendTask();
        //取消 校验重复清除任务
        this.duplicateFrameChecker.cancelAllClearTask();
    }

}
