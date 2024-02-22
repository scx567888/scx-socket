package cool.scx.socket;

import io.vertx.core.http.WebSocketBase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static cool.scx.socket.FrameCreator.createAckFrame;
import static cool.scx.socket.ScxSocketFrame.Type.*;
import static cool.scx.socket.ScxSocketFrame.fromJson;
import static cool.scx.socket.SendOptions.DEFAULT_SEND_OPTIONS;
import static cool.scx.util.StringUtils.isBlank;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

public class ScxSocket {

    protected final ConcurrentMap<Long, SendTask> sendTaskMap;
    protected final System.Logger logger = getLogger(this.getClass().getName());
    protected final ScxSocketOptions options;
    protected final String clientID;
    protected final DuplicateFrameChecker duplicateFrameChecker;
    private final FrameCreator frameCreator;
    private final ConcurrentMap<String, EventHandler> eventHandlerMap;
    private final ConcurrentMap<Long, Consumer<String>> responseCallbackMap;
    protected WebSocketBase webSocket;
    private Consumer<String> onMessage;
    private Consumer<Void> onClose;
    private Consumer<Throwable> onError;

    public ScxSocket(ScxSocketOptions options, String clientID) {
        this.sendTaskMap = new ConcurrentHashMap<>();
        this.duplicateFrameChecker = new DuplicateFrameChecker(options.getSeqIDClearTimeout());
        this.eventHandlerMap = new ConcurrentHashMap<>();
        this.responseCallbackMap = new ConcurrentHashMap<>();
        this.options = options;
        this.clientID = clientID;
        this.frameCreator = new FrameCreator();
    }

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
        callResponseCallbackAsync(socketFrame);
    }

    protected void doAck(ScxSocketFrame ackFrame) {
        var sendTask = sendTaskMap.get(ackFrame.ack_id);
        if (sendTask != null) {
            sendTask.clear();
        }
        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到 ACK : {1}", clientID, ackFrame.toJson());
        }
    }

    protected void doClose(Void v) {
        this.close();
        //呼叫 onClose 事件
        this.callOnClose(v);
    }

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
        //关闭 连接
        this.closeWebSocket();
        //取消所有重发任务
        this.cancelAllResendTask();
        //取消 校验重复清除任务
        this.duplicateFrameChecker.cancelAllClearTask();
    }

    public String clientID() {
        return clientID;
    }

    public void send(String content) {
        send(frameCreator.createMessageFrame(content, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
    }

    public void send(String content, SendOptions options) {
        send(frameCreator.createMessageFrame(content, options), options);
    }

    public void sendEvent(String eventName, String data) {
        send(frameCreator.createEventFrame(eventName, data, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
    }

    public void sendEvent(String eventName, String data, SendOptions options) {
        send(frameCreator.createEventFrame(eventName, data, options), options);
    }

    public void sendEvent(String eventName, String data, Consumer<String> responseCallback) {
        var eventFrame = frameCreator.createRequestFrame(eventName, data, DEFAULT_SEND_OPTIONS);
        setResponseCallback(eventFrame, responseCallback);
        send(eventFrame, DEFAULT_SEND_OPTIONS);
    }

    public void sendEvent(String eventName, String data, Consumer<String> responseCallback, SendOptions options) {
        var eventFrame = frameCreator.createRequestFrame(eventName, data, options);
        setResponseCallback(eventFrame, responseCallback);
        send(eventFrame, options);
    }

    protected void sendResponse(long ack_id, String responseData) {
        send(frameCreator.createResponseFrame(ack_id, responseData, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
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
            this.webSocket.close();
        }
    }

    public boolean isClosed() {
        return webSocket == null || webSocket.isClosed();
    }

    public final void onMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    public final void onClose(Consumer<Void> onClose) {
        this.onClose = onClose;
    }

    public final void onError(Consumer<Throwable> onError) {
        this.onError = onError;
    }

    public final void onEvent(String eventName, Consumer<String> onEvent) {
        this.eventHandlerMap.put(eventName, new EventHandler(onEvent));
    }

    public final void onEvent(String eventName, Function<String, String> onEvent) {
        this.eventHandlerMap.put(eventName, new EventHandler(onEvent));
    }

    public final void onEvent(String eventName, BiConsumer<String, ScxSocketRequest> onEvent) {
        this.eventHandlerMap.put(eventName, new EventHandler(onEvent));
    }

    public final void removeEvent(String eventName) {
        this.eventHandlerMap.remove(eventName);
    }

    private void callOnMessage(String message) {
        if (this.onMessage != null) {
            this.onMessage.accept(message);
        }
    }

    protected void callOnClose(Void v) {
        if (this.onClose != null) {
            this.onClose.accept(v);
        }
    }

    protected void callOnError(Throwable e) {
        if (this.onError != null) {
            this.onError.accept(e);
        }
    }

    private void callOnMessageAsync(String message) {
        if (this.onMessage != null) {
            Thread.ofVirtual().start(() -> this.onMessage.accept(message));
        }
    }

    private void callOnCloseAsync(Void v) {
        if (this.onClose != null) {
            Thread.ofVirtual().start(() -> this.onClose.accept(v));
        }
    }

    private void callOnErrorAsync(Throwable e) {
        if (this.onError != null) {
            Thread.ofVirtual().start(() -> this.onError.accept(e));
        }
    }

    protected final void callOnMessageWithCheckDuplicateAsync(ScxSocketFrame socketFrame) {
        if (this.onMessage != null && duplicateFrameChecker.checkDuplicate(socketFrame)) {
            Thread.ofVirtual().start(() -> this.onMessage.accept(socketFrame.payload));
        }
    }

    protected final void callOnEventWithCheckDuplicateAsync(ScxSocketFrame socketFrame) {
        var eventHandler = this.eventHandlerMap.get(socketFrame.event_name);
        if (eventHandler != null && duplicateFrameChecker.checkDuplicate(socketFrame)) {
            Thread.ofVirtual().start(() -> {
                switch (eventHandler.type) {
                    case 0 -> {
                        var event0 = eventHandler.event0();
                        event0.accept(socketFrame.payload);
                        if (socketFrame.need_response) {
                            sendResponse(socketFrame.seq_id, null);
                        }
                    }
                    case 1 -> {
                        var event1 = eventHandler.event1();
                        var responseData = event1.apply(socketFrame.payload);
                        if (socketFrame.need_response) {
                            sendResponse(socketFrame.seq_id, responseData);
                        }
                    }
                    case 2 -> {
                        var event2 = eventHandler.event2();
                        if (socketFrame.need_response) {
                            var scxSocketRequest = new ScxSocketRequest(this, socketFrame.seq_id);
                            event2.accept(socketFrame.payload, scxSocketRequest);
                        } else {
                            event2.accept(socketFrame.payload, null);
                        }
                    }
                }
            });
        }
    }

    protected final void setResponseCallback(ScxSocketFrame socketFrame, Consumer<String> responseCallback) {
        this.responseCallbackMap.put(socketFrame.seq_id, responseCallback);
    }

    protected void callResponseCallback(ScxSocketFrame socketFrame) {
        var responseCallback = this.responseCallbackMap.remove(socketFrame.ack_id);
        if (responseCallback != null) {
            responseCallback.accept(socketFrame.payload);
        }
    }

    protected void callResponseCallbackAsync(ScxSocketFrame socketFrame) {
        var responseCallback = this.responseCallbackMap.remove(socketFrame.ack_id);
        if (responseCallback != null) {
            Thread.ofVirtual().start(() -> responseCallback.accept(socketFrame.payload));
        }
    }

}
