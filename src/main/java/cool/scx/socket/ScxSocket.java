package cool.scx.socket;

import cool.scx.http.web_socket.ScxWebSocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static cool.scx.common.util.StringUtils.isBlank;
import static cool.scx.socket.ScxSocketFrame.Type.*;
import static cool.scx.socket.ScxSocketFrame.fromJson;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;


/**
 * ScxSocket
 *
 * @author scx567888
 * @version 0.0.1
 */
public class ScxSocket {

    protected final System.Logger logger = getLogger(this.getClass().getName());

    final ScxWebSocket webSocket;
    final String clientID;
    final ScxSocketOptions options;
    final ScxSocketStatus status;

    final ScheduledExecutorService scheduledExecutor;
    final Executor executor;

    private final ConcurrentMap<String, Consumer<ScxSocketRequest>> onEventMap;
    private Consumer<String> onMessage;
    private BiConsumer<Integer, String> onClose;
    private Consumer<Throwable> onError;

    ScxSocket(ScxWebSocket webSocket, String clientID, ScxSocketOptions options, ScxSocketStatus status) {
        this.webSocket = webSocket;
        this.clientID = clientID;
        this.options = options;
        this.status = status;
        this.scheduledExecutor = options.scheduledExecutor();
        this.executor = options.executor();
        this.onEventMap = new ConcurrentHashMap<>();
        this.onMessage = null;
        this.onClose = null;
        this.onError = null;
    }

    ScxSocket(ScxWebSocket webSocket, String clientID, ScxSocketOptions options) {
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

        try {

            this.webSocket.send(ackFrame.toJson());

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 ACK 成功 : {1}", clientID, ackFrame.toJson());
            }

        } catch (Exception e) {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 ACK 失败 : {1}", clientID, ackFrame.toJson(), e);
            }

        }
    }

    //*********************** 设置事件方法 ***********************

    public final void onMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    public final void onClose(BiConsumer<Integer, String> onClose) {
        this.onClose = onClose;
        //为了解决 绑定事件为完成是 连接就被关闭 从而无法触发 onClose 事件
        if (webSocket.isClosed()) {
            throw new IllegalStateException("WebSocket is closed");
        }
    }

    public final void onError(Consumer<Throwable> onError) {
        this.onError = onError;
        //为了解决 绑定事件为完成是 连接就被关闭 从而无法触发 onError 事件
        if (webSocket.isClosed()) {
            throw new IllegalStateException("WebSocket is closed");
        }
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

    protected void doClose(int code, String reason) {
        this.close();
        //呼叫 onClose 事件
        this._callOnClose(code, reason);
    }

    protected void doError(Throwable e) {
        this.close();
        //呼叫 onClose 事件
        this._callOnError(e);
    }

    //********************** 生命周期方法 ********************

    private void bind() {
        this.webSocket.onTextMessage((t, _) -> doSocketFrame(fromJson(t)));
        this.webSocket.onClose(this::doClose);
        this.webSocket.onError(this::doError);
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

            try {

                this.webSocket.close();

                //LOGGER
                if (logger.isLoggable(DEBUG)) {
                    logger.log(DEBUG, "CLIENT_ID : {0}, 关闭成功", clientID);
                }

            } catch (Exception e) {

                //LOGGER
                if (logger.isLoggable(DEBUG)) {
                    logger.log(DEBUG, "CLIENT_ID : {0}, 关闭失败", clientID, e);
                }

            }

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
            executor.execute(() -> this.onMessage.accept(message));
        }
    }

    private void _callOnClose(Integer code, String reason) {
        if (this.onClose != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            executor.execute(() -> this.onClose.accept(code, reason));
        }
    }

    private void _callOnError(Throwable e) {
        if (this.onError != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            executor.execute(() -> this.onError.accept(e));
        }
    }

    private void _callOnEvent(ScxSocketFrame socketFrame) {
        var onEvent = this.onEventMap.get(socketFrame.event_name);
        if (onEvent != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            executor.execute(() -> {
                var socketRequest = new ScxSocketRequest(this, socketFrame);
                onEvent.accept(socketRequest);
            });
        }
    }

}
