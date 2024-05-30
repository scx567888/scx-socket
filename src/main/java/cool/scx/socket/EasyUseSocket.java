package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.http.WebSocketBase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cool.scx.socket.Helper.fromJson;
import static cool.scx.socket.Helper.toJson;

/**
 * 便于使用的 Socket
 */
public class EasyUseSocket extends ScxSocket {

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    private static final RequestOptions DEFAULT_REQUEST_OPTIONS = new RequestOptions();

    EasyUseSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options, ScxSocketStatus status) {
        super(webSocket, clientID, options, status);
    }

    EasyUseSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options) {
        super(webSocket, clientID, options);
    }

    public final void send(String content) {
        send(content, DEFAULT_SEND_OPTIONS);
    }

    public final void send(Object data) {
        send(toJson(data), DEFAULT_SEND_OPTIONS);
    }

    public final void send(Object data, SendOptions options) {
        send(toJson(data), options);
    }

    public final void sendEvent(String eventName) {
        sendEvent(eventName, null, DEFAULT_SEND_OPTIONS);
    }

    public final void sendEvent(String eventName, String data) {
        sendEvent(eventName, data, DEFAULT_SEND_OPTIONS);
    }

    public final void sendEvent(String eventName, Object data) {
        sendEvent(eventName, toJson(data), DEFAULT_SEND_OPTIONS);
    }

    public final void sendEvent(String eventName, Object data, SendOptions options) {
        sendEvent(eventName, toJson(data), options);
    }

    public final void sendEvent(String eventName, BiConsumer<String, Throwable> responseCallback) {
        sendEvent(eventName, null, responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, BiConsumer<String, Throwable> responseCallback, RequestOptions options) {
        sendEvent(eventName, null, responseCallback, options);
    }

    public final void sendEvent(String eventName, String data, BiConsumer<String, Throwable> responseCallback) {
        sendEvent(eventName, data, responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, Object data, BiConsumer<String, Throwable> responseCallback) {
        sendEvent(eventName, toJson(data), responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, Object data, BiConsumer<String, Throwable> responseCallback, RequestOptions options) {
        sendEvent(eventName, toJson(data), responseCallback, options);
    }

    public final <T> void sendEvent(String eventName, BiConsumer<T, Throwable> responseCallback, TypeReference<T> tClass) {
        sendEvent(eventName, null, (s, e) -> responseCallback.accept(fromJson(s, tClass), e), DEFAULT_REQUEST_OPTIONS);
    }

    public final <T> void sendEvent(String eventName, BiConsumer<T, Throwable> responseCallback, RequestOptions options, TypeReference<T> tClass) {
        sendEvent(eventName, null, (s, e) -> responseCallback.accept(fromJson(s, tClass), e), options);
    }

    public final <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, TypeReference<T> tClass) {
        sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e), DEFAULT_REQUEST_OPTIONS);
    }

    public final <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, RequestOptions options, TypeReference<T> tClass) {
        sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e), options);
    }

    public final <T> void onEvent(String eventName, Consumer<T> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, (Consumer<String>) s -> onEvent.accept(fromJson(s, tClass)));
    }

    public final void onEvent(String eventName, Supplier<?> onEvent, TypeReference<?> tClass) {
        this.onEvent(eventName, () -> {
            var data = onEvent.get();
            return data instanceof String str ? str : toJson(data);
        });
    }

    public final <T> void onEvent(String eventName, Function<T, ?> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, s -> {
            var data = onEvent.apply(fromJson(s, tClass));
            return data instanceof String str ? str : toJson(data);
        });
    }

    public final <T> void onEvent(String eventName, BiConsumer<T, TypeRequest> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, (s, r) -> onEvent.accept(fromJson(s, tClass), new TypeRequest(r)));
    }

    public static class TypeRequest {

        private final ScxSocketRequest request;

        public TypeRequest(ScxSocketRequest r) {
            this.request = r;
        }

        public void response(String payload) {
            request.response(payload);
        }

        public void response(Object payload) {
            request.response(toJson(payload));
        }

    }

}
