package cool.scx.socket1.event;

import cool.scx.socket1.frame.ScxSocketFrame;
import cool.scx.socket1.request.ScxSocketRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 事件管理器 每个事件只允许注册一次
 */
public abstract class EventManager {

    protected final ConcurrentMap<String, EventHandler> eventHandlerMap;
    protected Consumer<String> onMessage;
    protected Consumer<Void> onClose;
    protected Consumer<Throwable> onError;

    public EventManager(EventManager oldEventManager) {
        this.eventHandlerMap = oldEventManager.eventHandlerMap;
        this.onMessage = oldEventManager.onMessage;
        this.onClose = oldEventManager.onClose;
        this.onError = oldEventManager.onError;
    }

    public EventManager() {
        this.eventHandlerMap = new ConcurrentHashMap<>();
    }

    public void onMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    public void onClose(Consumer<Void> onClose) {
        this.onClose = onClose;
    }

    public void onError(Consumer<Throwable> onError) {
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

    protected void _callOnMessage(String message) {
        if (this.onMessage != null) {
            this.onMessage.accept(message);
        }
    }

    protected void _callOnClose(Void v) {
        if (this.onClose != null) {
            this.onClose.accept(v);
        }
    }

    protected void _callOnError(Throwable e) {
        if (this.onError != null) {
            this.onError.accept(e);
        }
    }

    protected final void _callOnEvent(ScxSocketFrame socketFrame) {
        var eventHandler = this.eventHandlerMap.get(socketFrame.event_name);
        if (eventHandler != null) {
            this._callOnEvent(eventHandler, socketFrame);
        }
    }

    protected void _callOnMessageAsync(String message) {
        if (this.onMessage != null) {
            Thread.ofVirtual().start(() -> this.onMessage.accept(message));
        }
    }

    protected void _callOnCloseAsync(Void v) {
        if (this.onClose != null) {
            Thread.ofVirtual().start(() -> this.onClose.accept(v));
        }
    }

    protected void _callOnErrorAsync(Throwable e) {
        if (this.onError != null) {
            Thread.ofVirtual().start(() -> this.onError.accept(e));
        }
    }

    protected final void _callOnEventAsync(ScxSocketFrame socketFrame) {
        var eventHandler = this.eventHandlerMap.get(socketFrame.event_name);
        if (eventHandler != null) {
            Thread.ofVirtual().start(() -> _callOnEvent(eventHandler, socketFrame));
        }
    }

    private void _callOnEvent(EventHandler eventHandler, ScxSocketFrame socketFrame) {
        switch (eventHandler.type) {
            case 0 -> this._callOnEvent0(eventHandler.event0(), socketFrame);
            case 1 -> this._callOnEvent1(eventHandler.event1(), socketFrame);
            case 2 -> this._callOnEvent2(eventHandler.event2(), socketFrame);
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
            var scxSocketRequest = createRequest(socketFrame.seq_id);
            event2.accept(socketFrame.payload, scxSocketRequest);
        } else {
            event2.accept(socketFrame.payload, null);
        }
    }

    protected abstract void sendResponse(long ack_id, String responseData);

    protected abstract ScxSocketRequest createRequest(long ack_id);

    public final void removeEvent(String eventName) {
        this.eventHandlerMap.remove(eventName);
    }

}
