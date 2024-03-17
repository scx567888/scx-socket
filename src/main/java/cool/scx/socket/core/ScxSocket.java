package cool.scx.socket.core;

import cool.scx.socket.event.EventManager;
import cool.scx.socket.frame.FrameCreator;
import cool.scx.socket.frame.ScxSocketFrame;
import cool.scx.socket.helper.EasyUseHelper;
import cool.scx.socket.request.RequestManager;
import cool.scx.socket.request.ScxSocketRequest;
import cool.scx.socket.sender.SendOptions;
import io.vertx.core.http.WebSocketBase;

import java.util.function.Consumer;

import static cool.scx.common.util.StringUtils.isBlank;
import static cool.scx.socket.frame.FrameCreator.createAckFrame;
import static cool.scx.socket.frame.ScxSocketFrame.Type.*;
import static cool.scx.socket.frame.ScxSocketFrame.fromJson;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

public class ScxSocket extends EventManager implements EasyUseHelper {

    public final String clientID;
    protected final System.Logger logger = getLogger(this.getClass().getName());
    protected final ScxSocketOptions options;
    protected final ScxSocketStatus status;
    public WebSocketBase webSocket;

    public ScxSocket(ScxSocketOptions options, String clientID) {
        this.options = options;
        this.clientID = clientID;
        this.status = new ScxSocketStatus(options);
    }

    public ScxSocket(ScxSocket scxSocket) {
        super(scxSocket);
        this.options = scxSocket.options;
        this.clientID = scxSocket.clientID;
        this.status = scxSocket.status;
    }

    public void send(ScxSocketFrame socketFrame, SendOptions options) {
        this.status.frameSender.send(socketFrame, options, this);
    }

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
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到消息 : {1}", clientID, socketFrame.toJson());
        }

    }

    protected void doResponse(ScxSocketFrame socketFrame) {
        // ACK 应第一时间返回
        if (socketFrame.need_ack) {
            sendAck(socketFrame.seq_id);
        }
        requestManager().successAsync(socketFrame);
    }

    protected void doAck(ScxSocketFrame ackFrame) {
        this.status.frameSender.clearSendTask(ackFrame);

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到 ACK : {1}", clientID, ackFrame.toJson());
        }
    }

    protected void doClose(Void v) {
        this.close();
        //呼叫 onClose 事件
        this._callOnClose(v);
    }

    protected void doError(Throwable e) {
        this.close();
        //呼叫 onClose 事件
        this._callOnError(e);
    }

    protected void start(WebSocketBase webSocket) {
        close();
        //绑定事件
        this.bind(webSocket);
        //启动所有发送任务
        this.status.frameSender.startAllSendTask(this);
        //启动 校验重复清除任务
        this.status.duplicateFrameChecker.startAllClearTask();
    }

    public void close() {
        //关闭 连接
        this.closeWebSocket();
        //取消所有重发任务
        this.status.frameSender.cancelAllResendTask();
        //取消 校验重复清除任务
        this.status.duplicateFrameChecker.cancelAllClearTask();
    }

    public String clientID() {
        return clientID;
    }

    public void sendResponse(long ack_id, String responseData) {
        var sendOptions = new SendOptions();
        send(status.frameCreator.createResponseFrame(ack_id, responseData, sendOptions), sendOptions);
    }

    @Override
    protected ScxSocketRequest createRequest(long ack_id) {
        return new ScxSocketRequest(this, ack_id);
    }

    protected void sendAck(long ack_id) {
        var ackFrame = createAckFrame(ack_id);

        var sendAckFuture = this.webSocket.writeTextMessage(ackFrame.toJson());

        sendAckFuture.onSuccess(v -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 ACK 成功 : {1}", clientID, ackFrame.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 ACK 失败 : {1}", clientID, ackFrame.toJson(), c);
            }

        });


    }

    protected void bind(WebSocketBase webSocket) {
        this.webSocket = webSocket;
        this.webSocket.textMessageHandler(t -> doSocketFrame(fromJson(t)));
        this.webSocket.closeHandler(this::doClose);
        this.webSocket.exceptionHandler(this::doError);
    }

    protected void doSocketFrame(ScxSocketFrame socketFrame) {
        switch (socketFrame.type) {
            case MESSAGE -> doMessage(socketFrame);
            case RESPONSE -> doResponse(socketFrame);
            case ACK -> doAck(socketFrame);
        }
    }

    protected void closeWebSocket() {
        if (this.webSocket != null && !this.webSocket.isClosed()) {
            this.webSocket.close().onSuccess(c -> {

            });
        }
    }

    public boolean isClosed() {
        return webSocket == null || webSocket.isClosed();
    }

    public final void onClose(Consumer<Void> onClose) {
        super.onClose(onClose);
        //为了解决 绑定事件为完成是 连接就被关闭 从而无法触发 onClose 事件
        //此处绑定的意义在于如果当前 webSocket 已经被关闭则永远无法触发 onClose 事件
        //但是我们在这里调用 vertx 的绑定会触发异常 可以在外层进行 异常捕获然后进行对应的修改
        if (webSocket != null) {
            webSocket.closeHandler(this::doClose);
        }
    }

    public final void onError(Consumer<Throwable> onError) {
        super.onError(onError);
        //同 onClose
        if (webSocket != null) {
            webSocket.exceptionHandler(this::doError);
        }
    }

    private void callOnMessageWithCheckDuplicateAsync(ScxSocketFrame socketFrame) {
        if (this.status.duplicateFrameChecker.check(socketFrame)) {
            _callOnMessageAsync(socketFrame.payload);
        }
    }

    private void callOnEventWithCheckDuplicateAsync(ScxSocketFrame socketFrame) {
        if (this.status.duplicateFrameChecker.check(socketFrame)) {
            _callOnEventAsync(socketFrame);
        }
    }

    @Override
    public FrameCreator frameCreator() {
        return status.frameCreator;
    }

    @Override
    public RequestManager requestManager() {
        return status.requestManager;
    }

}
