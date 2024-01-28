package cool.scx.socket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class EventManager extends ScxSocketBase {

    protected final DuplicateFrameChecker duplicateFrameChecker;
    private final ConcurrentMap<String, EventHandler> eventHandlerMap;
    private final ConcurrentMap<Long, Consumer<String>> responseCallbackMap;
    private Consumer<String> onMessage;
    private Consumer<Void> onClose;
    private Consumer<Throwable> onError;

    public EventManager(ScxSocketOptions options, String clientID) {
        super(options, clientID);
        this.duplicateFrameChecker = new DuplicateFrameChecker(options.getSeqIDClearTimeout());
        this.eventHandlerMap = new ConcurrentHashMap<>();
        this.responseCallbackMap = new ConcurrentHashMap<>();
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

    @Override
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
