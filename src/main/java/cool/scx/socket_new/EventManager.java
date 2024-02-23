package cool.scx.socket_new;

import cool.scx.socket.ScxSocketRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class EventManager {

    private final ConcurrentMap<String, EventHandler> onEventMap;
    private Consumer<Void> onOpen;
    private Consumer<String> onMessage;
    private Consumer<Void> onClose;
    private Consumer<Throwable> onError;

    public EventManager() {
        this.onEventMap = new ConcurrentHashMap<>();
        this.onOpen = null;
        this.onMessage = null;
        this.onClose = null;
        this.onError = null;
    }

    public void onOpen(Consumer<Void> onOpen) {
        this.onOpen = onOpen;
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

    public void onEvent(String eventName, Consumer<String> onEvent) {
        this.onEventMap.put(eventName, new EventHandler(onEvent));
    }

    public void onEvent(String eventName, Function<String, String> onEvent) {
        this.onEventMap.put(eventName, new EventHandler(onEvent));
    }

    public void onEvent(String eventName, BiConsumer<String, ScxSocketRequest> onEvent) {
        this.onEventMap.put(eventName, new EventHandler(onEvent));
    }

    public void removeEvent(String eventName) {
        this.onEventMap.remove(eventName);
    }

    private void _callOnOpen(Void v) {
        if (this.onOpen != null) {
            this.onOpen.accept(v);
        }
    }

    private void _callOnMessage(String message) {
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

    protected void _callOnEvent(String eventName, String message) {
        var event = this.onEventMap.get(eventName);
        if (event != null) {
            event.event0().accept(message);
        }
    }

    private void _callOnOpenAsync(Void v) {
        if (this.onOpen != null) {
            Thread.ofVirtual().start(() -> this.onOpen.accept(v));
        }
    }

    private void _callOnMessageAsync(String message) {
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

    protected void _callOnEventAsync(String eventName, String message) {
        var event = this.onEventMap.get(eventName);
        if (event != null) {
            Thread.ofVirtual().start(() -> event.event0().accept(message));
        }
    }

}
