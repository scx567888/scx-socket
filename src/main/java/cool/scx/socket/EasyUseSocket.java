package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import cool.scx.http.web_socket.ScxWebSocket;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cool.scx.socket.Helper.toJson;

/**
 * 便于使用的 Socket
 *
 * @author scx567888
 * @version 0.0.1
 */
public class EasyUseSocket extends ScxSocket {

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    private static final RequestOptions DEFAULT_REQUEST_OPTIONS = new RequestOptions();

    EasyUseSocket(ScxWebSocket webSocket, String clientID, ScxSocketOptions options, ScxSocketStatus status) {
        super(webSocket, clientID, options, status);
    }

    EasyUseSocket(ScxWebSocket webSocket, String clientID, ScxSocketOptions options) {
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

    public final void sendEvent(String eventName, Consumer<ScxSocketResponse> responseCallback) {
        sendEvent(eventName, null, responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, Consumer<ScxSocketResponse> responseCallback, RequestOptions options) {
        sendEvent(eventName, null, responseCallback, options);
    }

    public final void sendEvent(String eventName, Object data, Consumer<ScxSocketResponse> responseCallback) {
        sendEvent(eventName, toJson(data), responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, Object data, Consumer<ScxSocketResponse> responseCallback, RequestOptions options) {
        sendEvent(eventName, toJson(data), responseCallback, options);
    }

    //************************ 方便直接使用 实例::方法 的形式调用 ***************************

    public final void onEvent(String eventName, Runnable onEvent) {
        this.onEvent(eventName, r -> {
            onEvent.run();
            if (r.socketFrame().need_response) {
                r.response(null);
            }
        });
    }

    public final <T> void onEvent(String eventName, Consumer<T> onEvent, TypeReference<T> valueTypeRef) {
        this.onEvent(eventName, r -> {
            onEvent.accept(r.payload(valueTypeRef));
            if (r.socketFrame().need_response) {
                r.response(null);
            }
        });
    }

    public final void onEvent(String eventName, Supplier<?> onEvent) {
        this.onEvent(eventName, r -> {
            var data = onEvent.get();
            if (r.socketFrame().need_response) {
                r.response(toJson(data));
            }
        });
    }

    public final <T> void onEvent(String eventName, Function<T, ?> onEvent, TypeReference<T> valueTypeRef) {
        this.onEvent(eventName, r -> {
            var data = onEvent.apply(r.payload(valueTypeRef));
            if (r.socketFrame().need_response) {
                r.response(toJson(data));
            }
        });
    }

}
