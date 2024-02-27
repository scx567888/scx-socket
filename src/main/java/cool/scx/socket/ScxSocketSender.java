package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static cool.scx.socket.ScxSocketHelper.fromJson;
import static cool.scx.socket.ScxSocketHelper.toJson;

/**
 * 便于使用
 */
public interface ScxSocketSender {

    void send(String content);

    void send(String content, SendOptions options);

    void sendEvent(String eventName, String data);

    void sendEvent(String eventName, String data, SendOptions options);

    void sendEvent(String eventName, String data, Consumer<String> responseCallback);

    void sendEvent(String eventName, String data, Consumer<String> responseCallback, SendOptions options);

    void onEvent(String eventName, Consumer<String> onEvent);

    void onEvent(String eventName, Function<String, String> onEvent);

    void onEvent(String eventName, BiConsumer<String, ScxSocketRequest> onEvent);

    default void send(Object data) {
        this.send(toJson(data));
    }

    default void send(Object data, SendOptions options) {
        this.send(toJson(data), options);
    }

    default void sendEvent(String eventName, Object data) {
        this.sendEvent(eventName, toJson(data));
    }

    default void sendEvent(String eventName, Object data, SendOptions options) {
        this.sendEvent(eventName, toJson(data), options);
    }

    default void sendEvent(String eventName, Object data, Consumer<String> responseCallback) {
        this.sendEvent(eventName, toJson(data), responseCallback);
    }

    default void sendEvent(String eventName, Object data, Consumer<String> responseCallback, SendOptions options) {
        this.sendEvent(eventName, toJson(data), responseCallback, options);
    }

    default <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, Class<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)));
    }

    default <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, SendOptions options, Class<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)), options);
    }

    default <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, TypeReference<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)));
    }

    default <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, SendOptions options, TypeReference<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)), options);
    }

    default <T> void onEvent(String eventName, Consumer<T> onEvent, Class<T> tClass) {
        this.onEvent(eventName, s -> {
            onEvent.accept(fromJson(s, tClass));
        });
    }

    default <T> void onEvent(String eventName, Function<T, ?> onEvent, Class<T> tClass) {
        this.onEvent(eventName, s -> {
            var data = onEvent.apply(fromJson(s, tClass));
            if (data instanceof String str) {
                return str;
            } else {
                return toJson(data);
            }
        });
    }

    default <T> void onEvent(String eventName, Consumer<T> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, s -> {
            onEvent.accept(fromJson(s, tClass));
        });
    }

    default <T> void onEvent(String eventName, Function<T, ?> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, s -> {
            var data = onEvent.apply(fromJson(s, tClass));
            if (data instanceof String str) {
                return str;
            } else {
                return toJson(data);
            }
        });
    }

    default <T> void onEvent(String eventName, BiConsumer<T, TypeRequest> onEvent, Class<T> tClass) {
        this.onEvent(eventName, (s, r) -> {
            onEvent.accept(fromJson(s, tClass), new TypeRequest(r));
        });
    }

    default <T> void onEvent(String eventName, BiConsumer<T, TypeRequest> onEvent, TypeReference<T> tClass) {
        this.onEvent(eventName, (s, r) -> {
            onEvent.accept(fromJson(s, tClass), new TypeRequest(r));
        });
    }

    class TypeRequest {

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
