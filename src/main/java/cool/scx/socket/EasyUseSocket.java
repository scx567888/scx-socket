package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.http.WebSocketBase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static cool.scx.socket.Helper.fromJson;
import static cool.scx.socket.Helper.toJson;

/**
 * 便于使用的 Socket
 */
class EasyUseSocket extends ScxSocket {

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    private static final RequestOptions DEFAULT_REQUEST_OPTIONS = new RequestOptions();

    public EasyUseSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options, ScxSocketStatus status) {
        super(webSocket, clientID, options, status);
    }

    public EasyUseSocket(WebSocketBase webSocket, String clientID, ScxSocketOptions options) {
        super(webSocket, clientID, options);
    }

    public final void send(String content) {
        send(status.frameCreator.createMessageFrame(content, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
    }

    public final void send(String content, SendOptions options) {
        send(status.frameCreator.createMessageFrame(content, options), options);
    }

    public final void sendEvent(String eventName, String data) {
        send(status.frameCreator.createEventFrame(eventName, data, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
    }

    public final void sendEvent(String eventName, String data, SendOptions options) {
        send(status.frameCreator.createEventFrame(eventName, data, options), options);
    }

    public final void sendEvent(String eventName, String data, BiConsumer<String, Throwable> responseCallback) {
        var eventFrame = status.frameCreator.createRequestFrame(eventName, data, DEFAULT_REQUEST_OPTIONS);
        status.requestManager.setResponseCallback(eventFrame, responseCallback, DEFAULT_REQUEST_OPTIONS);
        send(eventFrame, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, String data, BiConsumer<String, Throwable> responseCallback, RequestOptions options) {
        var eventFrame = status.frameCreator.createRequestFrame(eventName, data, options);
        status.requestManager.setResponseCallback(eventFrame, responseCallback, options);
        send(eventFrame, options);
    }

    public final void send(Object data) {
        this.send(toJson(data));
    }

    public final void send(Object data, SendOptions options) {
        this.send(toJson(data), options);
    }

    public final void sendEvent(String eventName, Object data) {
        this.sendEvent(eventName, toJson(data));
    }

    public final void sendEvent(String eventName, Object data, SendOptions options) {
        this.sendEvent(eventName, toJson(data), options);
    }

    public final void sendEvent(String eventName, Object data, BiConsumer<String, Throwable> responseCallback) {
        this.sendEvent(eventName, toJson(data), responseCallback);
    }

    public final void sendEvent(String eventName, Object data, BiConsumer<String, Throwable> responseCallback, RequestOptions options) {
        this.sendEvent(eventName, toJson(data), responseCallback, options);
    }

    public final <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, Class<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e));
    }

    public final <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, RequestOptions options, Class<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e), options);
    }

    public final <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, TypeReference<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e));
    }

    public final <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, RequestOptions options, TypeReference<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e), options);
    }

    public final <T> void onEvent(String eventName, Consumer<T> onEvent, Class<T> tClass) {
        this.onEvent(eventName, s -> {
            onEvent.accept(fromJson(s, tClass));
        });
    }

    public final <T> void onEvent(String eventName, Function<T, ?> onEvent, Class<T> tClass) {
        this.onEvent(eventName, s -> {
            var data = onEvent.apply(fromJson(s, tClass));
            if (data instanceof String str) {
                return str;
            } else {
                return toJson(data);
            }
        });
    }

    public final <T> void onEvent(String eventName, Consumer<T> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, s -> {
            onEvent.accept(fromJson(s, tClass));
        });
    }

    public final <T> void onEvent(String eventName, Function<T, ?> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, s -> {
            var data = onEvent.apply(fromJson(s, tClass));
            if (data instanceof String str) {
                return str;
            } else {
                return toJson(data);
            }
        });
    }

    public final <T> void onEvent(String eventName, BiConsumer<T, TypeRequest> onEvent, Class<T> tClass) {
        this.onEvent(eventName, (s, r) -> {
            onEvent.accept(fromJson(s, tClass), new TypeRequest(r));
        });
    }

    public final <T> void onEvent(String eventName, BiConsumer<T, TypeRequest> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, (s, r) -> {
            onEvent.accept(fromJson(s, tClass), new TypeRequest(r));
        });
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
