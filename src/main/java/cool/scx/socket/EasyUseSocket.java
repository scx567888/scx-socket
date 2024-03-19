package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static cool.scx.socket.Helper.fromJson;
import static cool.scx.socket.Helper.toJson;

/**
 * 便于使用的 接口
 */
public interface EasyUseSocket {

    SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    RequestOptions DEFAULT_REQUEST_OPTIONS = new RequestOptions();

    FrameCreator frameCreator();

    RequestManager requestManager();

    void send(ScxSocketFrame socketFrame, SendOptions options);

    void onEvent(String eventName, Consumer<String> onEvent);

    void onEvent(String eventName, Function<String, String> onEvent);

    void onEvent(String eventName, BiConsumer<String, ScxSocketRequest> onEvent);

    default void send(String content) {
        send(frameCreator().createMessageFrame(content, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
    }

    default void send(String content, SendOptions options) {
        send(frameCreator().createMessageFrame(content, options), options);
    }

    default void sendEvent(String eventName, String data) {
        send(frameCreator().createEventFrame(eventName, data, DEFAULT_SEND_OPTIONS), DEFAULT_SEND_OPTIONS);
    }

    default void sendEvent(String eventName, String data, SendOptions options) {
        send(frameCreator().createEventFrame(eventName, data, options), options);
    }

    default void sendEvent(String eventName, String data, BiConsumer<String, Throwable> responseCallback) {
        var eventFrame = frameCreator().createRequestFrame(eventName, data, DEFAULT_REQUEST_OPTIONS);
        requestManager().setResponseCallback(eventFrame, responseCallback, DEFAULT_REQUEST_OPTIONS);
        send(eventFrame, DEFAULT_REQUEST_OPTIONS);
    }

    default void sendEvent(String eventName, String data, BiConsumer<String, Throwable> responseCallback, RequestOptions options) {
        var eventFrame = frameCreator().createRequestFrame(eventName, data, options);
        requestManager().setResponseCallback(eventFrame, responseCallback, options);
        send(eventFrame, options);
    }

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

    default void sendEvent(String eventName, Object data, BiConsumer<String, Throwable> responseCallback) {
        this.sendEvent(eventName, toJson(data), responseCallback);
    }

    default void sendEvent(String eventName, Object data, BiConsumer<String, Throwable> responseCallback, RequestOptions options) {
        this.sendEvent(eventName, toJson(data), responseCallback, options);
    }

    default <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, Class<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e));
    }

    default <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, RequestOptions options, Class<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e), options);
    }

    default <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, TypeReference<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e));
    }

    default <T> void sendEvent(String eventName, Object data, BiConsumer<T, Throwable> responseCallback, RequestOptions options, TypeReference<T> tClass) {
        this.sendEvent(eventName, toJson(data), (s, e) -> responseCallback.accept(fromJson(s, tClass), e), options);
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
