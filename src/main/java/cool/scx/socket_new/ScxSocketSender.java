package cool.scx.socket_new;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static cool.scx.socket_new.SendOptions.DEFAULT_SEND_OPTIONS;

public abstract class ScxSocketSender extends ScxSocketEventManager {

    protected final FrameCreator frameCreator;
    private final ConcurrentMap<Long, Consumer<String>> responseCallbackMap;

    public ScxSocketSender() {
        this.frameCreator = new FrameCreator();
        this.responseCallbackMap = new ConcurrentHashMap<>();
    }

    protected abstract void send(ScxSocketFrame socketFrame, SendOptions options);

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
        _setResponseCallback(eventFrame, responseCallback);
        send(eventFrame, SendOptions.DEFAULT_SEND_OPTIONS);
    }

    public void sendEvent(String eventName, String data, Consumer<String> responseCallback, SendOptions options) {
        var eventFrame = frameCreator.createRequestFrame(eventName, data, options);
        _setResponseCallback(eventFrame, responseCallback);
        send(eventFrame, options);
    }

    protected void _sendResponse(long ack_id, String responseData) {
        send(frameCreator.createResponseFrame(ack_id, responseData, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
    }

    protected final void _setResponseCallback(ScxSocketFrame socketFrame, Consumer<String> responseCallback) {
        this.responseCallbackMap.put(socketFrame.seq_id, responseCallback);
    }

    protected void _callResponseCallback(ScxSocketFrame socketFrame) {
        var responseCallback = this.responseCallbackMap.remove(socketFrame.ack_id);
        if (responseCallback != null) {
            responseCallback.accept(socketFrame.payload);
        }
    }

    protected void _callResponseCallbackAsync(ScxSocketFrame socketFrame) {
        var responseCallback = this.responseCallbackMap.remove(socketFrame.ack_id);
        if (responseCallback != null) {
            Thread.ofVirtual().start(() -> responseCallback.accept(socketFrame.payload));
        }
    }

}
