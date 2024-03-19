package cool.scx.socket;

import cool.scx.socket.frame.FrameCreator;
import cool.scx.socket.frame.ScxSocketFrame;
import cool.scx.socket.helper.EasyUseSocket;
import cool.scx.socket.request.RequestManager;
import cool.scx.socket.request.ScxSocketRequest;
import cool.scx.socket.sender.SendOptions;
import io.vertx.core.http.WebSocketBase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static cool.scx.common.util.StringUtils.isBlank;
import static cool.scx.socket.frame.FrameCreator.createAckFrame;
import static cool.scx.socket.frame.ScxSocketFrame.Type.*;
import static cool.scx.socket.frame.ScxSocketFrame.fromJson;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

public class ScxSocket implements EasyUseSocket {

    protected final System.Logger logger = getLogger(this.getClass().getName());

    private final WebSocketBase webSocket;
    private final String clientID;
    private final ScxSocketOptions options;
    final ScxSocketStatus status;
    private final ConcurrentMap<String, EventHandler> eventHandlerMap;
    private Consumer<String> onMessage;
    private Consumer<Void> onClose;
    private Consumer<Throwable> onError;

    public ScxSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options, ScxSocketStatus status) {
        this.webSocket = webSocket;
        this.clientID = clientID;
        this.options = options;
        this.status = status;
        this.eventHandlerMap = new ConcurrentHashMap<>();
        this.onMessage = null;
        this.onClose = null;
        this.onError = null;
    }

    public ScxSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options) {
        this(webSocket, clientID, options, new ScxSocketStatus(options));
    }

    //***************** 对外属性 ******************

    @Override
    public final FrameCreator frameCreator() {
        return this.status.frameCreator;
    }

    @Override
    public final RequestManager requestManager() {
        return this.status.requestManager;
    }

    public final String clientID() {
        return clientID;
    }

    //***************** 发送事件 ********************

    @Override
    public final void send(ScxSocketFrame socketFrame, SendOptions options) {
        this.status.frameSender.send(socketFrame, options, this);
    }

    private void sendResponse(long ack_id, String responseData) {
        var sendOptions = new SendOptions();
        send(status.frameCreator.createResponseFrame(ack_id, responseData, sendOptions), sendOptions);
    }

    private void sendAck(long ack_id) {
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

    @Override
    public final void onEvent(String eventName, Consumer<String> onEvent) {
        this.eventHandlerMap.put(eventName, new EventHandler(onEvent));
    }

    @Override
    public final void onEvent(String eventName, Function<String, String> onEvent) {
        this.eventHandlerMap.put(eventName, new EventHandler(onEvent));
    }

    @Override
    public final void onEvent(String eventName, BiConsumer<String, ScxSocketRequest> onEvent) {
        this.eventHandlerMap.put(eventName, new EventHandler(onEvent));
    }

    public final void removeEvent(String eventName) {
        this.eventHandlerMap.remove(eventName);
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
        requestManager().successAsync(socketFrame);
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
            this.webSocket.close().onSuccess(c -> {

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
            this.onMessage.accept(message);
        }
    }

    private void _callOnClose(Void v) {
        if (this.onClose != null) {
            this.onClose.accept(v);
        }
    }

    private void _callOnError(Throwable e) {
        if (this.onError != null) {
            this.onError.accept(e);
        }
    }

    private void _callOnEvent(ScxSocketFrame socketFrame) {
        var eventHandler = this.eventHandlerMap.get(socketFrame.event_name);
        if (eventHandler != null) {
            switch (eventHandler.type) {
                case 0 -> this._callOnEvent0(eventHandler.event0(), socketFrame);
                case 1 -> this._callOnEvent1(eventHandler.event1(), socketFrame);
                case 2 -> this._callOnEvent2(eventHandler.event2(), socketFrame);
            }
        }
    }

    private void _callOnEvent0(Consumer<String> event0, ScxSocketFrame socketFrame) {
        event0.accept(socketFrame.payload);
        if (socketFrame.need_response) {
            sendResponse(socketFrame.seq_id, null);
        }
    }

    private void _callOnEvent1(Function<String, String> event1, ScxSocketFrame socketFrame) {
        var responseData = event1.apply(socketFrame.payload);
        if (socketFrame.need_response) {
            sendResponse(socketFrame.seq_id, responseData);
        }
    }

    private void _callOnEvent2(BiConsumer<String, ScxSocketRequest> event2, ScxSocketFrame socketFrame) {
        if (socketFrame.need_response) {
            var scxSocketRequest = new ScxSocketRequest(this, socketFrame.seq_id);
            event2.accept(socketFrame.payload, scxSocketRequest);
        } else {
            event2.accept(socketFrame.payload, null);
        }
    }

}
