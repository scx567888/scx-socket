package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import cool.scx.util.ObjectUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static cool.scx.util.ScxExceptionHelper.wrap;

/**
 * 便于使用
 */
public abstract class TypeConverter extends PingPongManager {

    private final static JsonMapper jsonMapper = ObjectUtils.jsonMapper();

    public TypeConverter(ScxSocketOptions options, String clientID) {
        super(options, clientID);
    }

    private static String toJson(Object data) {
        return wrap(() -> jsonMapper.writeValueAsString(data));
    }

    private static <T> T fromJson(String json, Class<T> tClass) {
        return wrap(() -> jsonMapper.readValue(json, tClass));
    }

    private static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        return wrap(() -> jsonMapper.readValue(json, valueTypeRef));
    }

    public void send(Object data) {
        super.send(toJson(data));
    }

    public void send(Object data, SendOptions options) {
        super.send(toJson(data), options);
    }

    public void sendEvent(String eventName, Object data) {
        super.sendEvent(eventName, toJson(data));
    }

    public void sendEvent(String eventName, Object data, SendOptions options) {
        super.sendEvent(eventName, toJson(data), options);
    }

    public void sendEvent(String eventName, Object data, Consumer<String> responseCallback) {
        super.sendEvent(eventName, toJson(data), responseCallback);
    }

    public void sendEvent(String eventName, Object data, Consumer<String> responseCallback, SendOptions options) {
        super.sendEvent(eventName, toJson(data), responseCallback, options);
    }

    public <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, Class<T> tClass) {
        super.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)));
    }

    public <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, SendOptions options, Class<T> tClass) {
        super.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)), options);
    }

    public <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, TypeReference<T> tClass) {
        super.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)));
    }

    public <T> void sendEvent(String eventName, Object data, Consumer<T> responseCallback, SendOptions options, TypeReference<T> tClass) {
        super.sendEvent(eventName, toJson(data), (s) -> responseCallback.accept(fromJson(s, tClass)), options);
    }

    public <T> void onEvent(String eventName, Consumer<T> onEvent, Class<T> tClass) {
        super.onEvent(eventName, s -> {
            onEvent.accept(fromJson(s, tClass));
        });
    }

    public <T> void onEvent(String eventName, Function<T, ?> onEvent, Class<T> tClass) {
        super.onEvent(eventName, s -> {
            var data = onEvent.apply(fromJson(s, tClass));
            if (data instanceof String str) {
                return str;
            } else {
                return toJson(data);
            }
        });
    }

    public <T> void onEvent(String eventName, Consumer<T> onEvent, TypeReference<T> tClass) {
        super.onEvent(eventName, s -> {
            onEvent.accept(fromJson(s, tClass));
        });
    }

    public <T> void onEvent(String eventName, Function<T, ?> onEvent, TypeReference<T> tClass) {
        super.onEvent(eventName, s -> {
            var data = onEvent.apply(fromJson(s, tClass));
            if (data instanceof String str) {
                return str;
            } else {
                return toJson(data);
            }
        });
    }

    public <T> void onEvent(String eventName, BiConsumer<T, TypeRequest> onEvent, Class<T> tClass) {
        super.onEvent(eventName, (s, r) -> {
            onEvent.accept(fromJson(s, tClass), new TypeRequest(r));
        });
    }

    public <T> void onEvent(String eventName, BiConsumer<T, TypeRequest> onEvent, TypeReference<T> tClass) {
        super.onEvent(eventName, (s, r) -> {
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
