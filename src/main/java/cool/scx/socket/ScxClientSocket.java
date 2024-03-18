package cool.scx.socket;

import cool.scx.socket.core.ScxSocket;
import cool.scx.socket.core.ScxSocketStatus;
import io.vertx.core.http.WebSocket;

/**
 * 客户端 Socket 对象
 */
public final class ScxClientSocket extends ScxSocket {

    private final ScxSocketClient socketClient;

    public ScxClientSocket(WebSocket webSocket, String clientID, ScxSocketClient socketClient) {
        super(webSocket, clientID, socketClient.clientOptions);
        this.socketClient = socketClient;
    }

    public ScxClientSocket(WebSocket webSocket, String clientID, ScxSocketClient socketClient, ScxSocketStatus status) {
        super(webSocket, clientID, socketClient.clientOptions, status);
        this.socketClient = socketClient;
    }

}
