package cool.scx.socket;

import io.vertx.core.http.WebSocketBase;

import java.util.function.Consumer;

import static cool.scx.socket.FrameCreator.createAckFrame;
import static cool.scx.socket.ScxSocketFrame.fromJson;
import static cool.scx.socket.ScxSocketFrameType.*;
import static cool.scx.socket.SendOptions.DEFAULT_SEND_OPTIONS;
import static java.lang.System.Logger;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

public abstract class ScxSocketBase {

    protected final Logger logger = getLogger(this.getClass().getName());
    protected final ScxSocketOptions options;
    private final FrameCreator frameCreator;
    protected WebSocketBase webSocket;

    protected ScxSocketBase(ScxSocketOptions options) {
        this.options = options;
        this.frameCreator = new FrameCreator();
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
                logger.log(DEBUG, "发送 ACK 成功 : {0}", ackFrame.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "发送 ACK 失败 : {0}", ackFrame.toJson(), c);
            }

        });


    }

    protected void bind(WebSocketBase webSocket) {
        this.webSocket = webSocket;
        this.webSocket.textMessageHandler(t -> doSocketFrame(fromJson(t)));
        this.webSocket.closeHandler(this::doClose);
        this.webSocket.exceptionHandler(this::doError);
    }

    protected void removeBind() {
        if (this.webSocket != null && !this.webSocket.isClosed()) {
            this.webSocket.textMessageHandler(null);
            this.webSocket.closeHandler(null);
            this.webSocket.exceptionHandler(null);
        }
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

    protected abstract void send(ScxSocketFrame socketFrame, SendOptions options);

    protected abstract void setResponseCallback(ScxSocketFrame socketFrame, Consumer<String> responseCallback);

    protected abstract void doMessage(ScxSocketFrame socketFrame);

    protected abstract void doResponse(ScxSocketFrame socketFrame);

    protected abstract void doAck(ScxSocketFrame ackFrame);

    protected abstract void doClose(Void v);

    protected abstract void doError(Throwable e);

}
