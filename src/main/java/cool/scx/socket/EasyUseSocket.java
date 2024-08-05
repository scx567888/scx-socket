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

    public final void sendEvent(String eventName, Consumer<ScxSocketResponse> responseCallback) {
        sendEvent(eventName, null, responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, Consumer<ScxSocketResponse> responseCallback, RequestOptions options) {
        sendEvent(eventName, null, responseCallback, options);
    }

    public final void sendEvent(String eventName, String data, Consumer<ScxSocketResponse> responseCallback) {
        sendEvent(eventName, data, responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, Object data, Consumer<ScxSocketResponse> responseCallback) {
        sendEvent(eventName, toJson(data), responseCallback, DEFAULT_REQUEST_OPTIONS);
    }

    public final void sendEvent(String eventName, Object data, Consumer<ScxSocketResponse> responseCallback, RequestOptions options) {
        sendEvent(eventName, toJson(data), responseCallback, options);
    }

}
