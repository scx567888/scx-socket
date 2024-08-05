package cool.scx.socket;

import io.vertx.core.http.WebSocketBase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static cool.scx.common.util.StringUtils.isBlank;
import static cool.scx.socket.ScxSocketFrame.Type.*;
import static cool.scx.socket.ScxSocketFrame.fromJson;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

public class ScxSocket {

    protected final System.Logger logger = getLogger(this.getClass().getName());

    final WebSocketBase webSocket;
    final String clientID;
    final ScxSocketOptions options;
    final ScxSocketStatus status;

    private final ConcurrentMap<String, Consumer<ScxSocketRequest>> onEventMap;
    private Consumer<String> onMessage;
    private Consumer<Void> onClose;
    private Consumer<Throwable> onError;

    ScxSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options, ScxSocketStatus status) {
        this.webSocket = webSocket;
        this.clientID = clientID;
        this.options = options;
        this.status = status;
        this.onEventMap = new ConcurrentHashMap<>();
        this.onMessage = null;
        this.onClose = null;
        this.onError = null;
    }

    ScxSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options) {
        this(webSocket, clientID, options, new ScxSocketStatus(options));
    }

    //***************** 对外属性 ******************

    public final String clientID() {
        return clientID;
    }

    //***************** 发送事件 ********************

    public final void send(ScxSocketFrame socketFrame, SendOptions options) {
        this.status.frameSender.send(socketFrame, options, this);
    }

    public final void send(String content, SendOptions options) {
        send(status.frameCreator.createMessageFrame(content, options), options);
    }

    public final void sendEvent(String eventName, String data, SendOptions options) {
        send(status.frameCreator.createEventFrame(eventName, data, options), options);
    }

    public final void sendEvent(String eventName, String data, Consumer<ScxSocketResponse> responseCallback, RequestOptions options) {
        var eventFrame = status.frameCreator.createRequestFrame(eventName, data, options);
        status.requestManager.setResponseCallback(eventFrame, responseCallback, options);
        send(eventFrame, options);
    }

    public final void sendResponse(long ack_id, String responseData) {
        var sendOptions = new SendOptions();
        var responseFrame = status.frameCreator.createResponseFrame(ack_id, responseData, sendOptions);
        send(responseFrame, sendOptions);
    }

    private void sendAck(long ack_id) {
        var ackFrame = status.frameCreator.createAckFrame(ack_id);
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

    //*********************** 设置事件方法 ***********************

    public final void onMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    public final void onClose(Consumer<Void> onClose) {
        this.onClose = onClose;
        //为了解决 绑定事件为完成是 连接就被关闭 从而无法触发 onClose 事件
        //此处绑定的意义在于如果当前 webSocket 已经被关闭则永远无法触发 onClose 事件
        //但是我们在这里调用 vertx 的绑定会触发异常 可以在外层进行 异常捕获然后进行对应的修改
        this.webSocket.closeHandler(this::doClose);
    }

    public final void onError(Consumer<Throwable> onError) {
        this.onError = onError;
        //同 onClose
        this.webSocket.exceptionHandler(this::doError);
    }

    public final void onEvent(String eventName, Consumer<ScxSocketRequest> onEvent) {
        this.onEventMap.put(eventName, onEvent);
    }

    public final void removeEvent(String eventName) {
        this.onEventMap.remove(eventName);
    }

    //********************* 内部事件 *********************

    protected void doSocketFrame(ScxSocketFrame socketFrame) {
        switch (socketFrame.type) {
            case MESSAGE -> doMessage(socketFrame);
            case RESPONSE -> doResponse(socketFrame);
            case ACK -> doAck(socketFrame);
        }
    }

    private void doMessage(ScxSocketFrame socketFrame) {
        // ACK 应第一时间返回
        if (socketFrame.need_ack) {
            sendAck(socketFrame.seq_id);
        }
        if (isBlank(socketFrame.event_name)) {
            callOnMessageWithCheckDuplicate(socketFrame);
        } else {
            callOnEventWithCheckDuplicate(socketFrame);
        }

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到消息 : {1}", clientID, socketFrame.toJson());
        }

    }

    private void doResponse(ScxSocketFrame socketFrame) {
        // ACK 应第一时间返回
        if (socketFrame.need_ack) {
            sendAck(socketFrame.seq_id);
        }
        status.requestManager.success(socketFrame);
    }

    private void doAck(ScxSocketFrame ackFrame) {
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

    //********************** 生命周期方法 ********************

    private void bind() {
        this.webSocket.textMessageHandler(t -> doSocketFrame(fromJson(t)));
        this.webSocket.closeHandler(this::doClose);
        this.webSocket.exceptionHandler(this::doError);
    }

    protected void start() {
        //绑定事件
        this.bind();
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

    protected void closeWebSocket() {
        if (!this.webSocket.isClosed()) {
            var closeFuture = this.webSocket.close();

            closeFuture.onSuccess(c -> {

                //LOGGER
                if (logger.isLoggable(DEBUG)) {
                    logger.log(DEBUG, "CLIENT_ID : {0}, 关闭成功", clientID);
                }

            }).onFailure(e -> {

                //LOGGER
                if (logger.isLoggable(DEBUG)) {
                    logger.log(DEBUG, "CLIENT_ID : {0}, 关闭失败", clientID, e);
                }

            });
        }
    }

    public boolean isClosed() {
        return webSocket.isClosed();
    }

    //******************* 调用事件 ********************

    private void callOnMessageWithCheckDuplicate(ScxSocketFrame socketFrame) {
        if (this.status.duplicateFrameChecker.check(socketFrame)) {
            _callOnMessage(socketFrame.payload);
        }
    }

    private void callOnEventWithCheckDuplicate(ScxSocketFrame socketFrame) {
        if (this.status.duplicateFrameChecker.check(socketFrame)) {
            _callOnEvent(socketFrame);
        }
    }

    private void _callOnMessage(String message) {
        if (this.onMessage != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            Thread.ofVirtual().name("scx-socket-call-on-message").start(() -> this.onMessage.accept(message));
        }
    }

    private void _callOnClose(Void v) {
        if (this.onClose != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            Thread.ofVirtual().name("scx-socket-call-on-close").start(() -> this.onClose.accept(v));
        }
    }

    private void _callOnError(Throwable e) {
        if (this.onError != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            Thread.ofVirtual().name("scx-socket-call-on-error").start(() -> this.onError.accept(e));
        }
    }

    private void _callOnEvent(ScxSocketFrame socketFrame) {
        var onEvent = this.onEventMap.get(socketFrame.event_name);
        if (onEvent != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            Thread.ofVirtual().name("scx-socket-call-on-event").start(() -> {
                var socketRequest = new ScxSocketRequest(this, socketFrame);
                onEvent.accept(socketRequest);
            });
        }
    }

}
