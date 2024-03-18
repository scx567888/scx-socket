package cool.scx.socket;

import cool.scx.common.util.SingleListenerFuture;
import io.netty.util.Timeout;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketConnectOptions;

import java.util.function.Consumer;

import static cool.scx.common.util.RandomUtils.randomUUID;
import static cool.scx.socket.helper.Helper.createConnectOptions;
import static cool.scx.socket.helper.Helper.setTimeout;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

public final class ScxSocketClient {

    private static final System.Logger logger = getLogger(ScxSocketClient.class.getName());

    private final WebSocketConnectOptions connectOptions;
    private final WebSocketClient webSocketClient;
    private final String clientID;
    private final ScxSocketClientOptions options;

    private ScxClientSocket clientSocket;
    private Consumer<ScxClientSocket> onConnect;
    private SingleListenerFuture<WebSocket> connectFuture;
    private Timeout reconnectTimeout;

    public ScxSocketClient(String uri, WebSocketClient webSocketClient, String clientID, ScxSocketClientOptions options) {
        this.connectOptions = createConnectOptions(uri, clientID);
        this.webSocketClient = webSocketClient;
        this.clientID = clientID;
        this.options = options;
    }

    public ScxSocketClient(String uri, WebSocketClient webSocketClient, ScxSocketClientOptions options) {
        this(uri, webSocketClient, randomUUID(), options);
    }

    public ScxSocketClient(String uri, WebSocketClient webSocketClient, String clientID) {
        this(uri, webSocketClient, clientID, new ScxSocketClientOptions());
    }

    public ScxSocketClient(String uri, WebSocketClient webSocketClient) {
        this(uri, webSocketClient, randomUUID(), new ScxSocketClientOptions());
    }

    public void onConnect(Consumer<ScxClientSocket> onConnect) {
        this.onConnect = onConnect;
    }

    private void _callOnConnect(ScxClientSocket clientConnect) {
        if (this.onConnect != null) {
            this.onConnect.accept(clientConnect);
        }
    }

    public void connect() {
        //当前已经存在一个连接中的任务
        if (this.connectFuture != null && !this.connectFuture.isComplete()) {
            return;
        }
        //关闭上一次连接
        this._closeOldSocket();
        this.connectFuture = new SingleListenerFuture<>(webSocketClient.connect(connectOptions));
        this.connectFuture.onSuccess((webSocket) -> {
            this.clientSocket = clientSocket != null ?
                    new ScxClientSocket(webSocket, clientID, this, clientSocket.status) :
                    new ScxClientSocket(webSocket, clientID, this);
            this.clientSocket.start();
            this._callOnConnect(clientSocket);
        }).onFailure((v) -> this.reconnect());
    }

    void reconnect() {
        //如果当前已经存在一个重连进程 则不进行重连
        if (this.reconnectTimeout != null) {
            return;
        }
        logger.log(DEBUG, "WebSocket 重连中... CLIENT_ID : {0}",clientID);
        this.reconnectTimeout = setTimeout(() -> {  //没连接上会一直重连，设置延迟为5000毫秒避免请求过多
            this.reconnectTimeout = null;
            this.connect();
        }, options.getReconnectTimeout());
    }

    void cancelReconnect() {
        if (this.reconnectTimeout != null) {
            this.reconnectTimeout.cancel();
            this.reconnectTimeout = null;
        }
    }

    void removeConnectFuture() {
        if (this.connectFuture != null) {
            //只有当未完成的时候才设置
            if (!this.connectFuture.isComplete()) {
                this.connectFuture.onSuccess(WebSocketBase::close).onFailure(null);
            }
            this.connectFuture = null;
        }
    }

    private void _closeOldSocket() {
        if (this.clientSocket != null) {
            this.clientSocket.close();
        }
    }

    ScxSocketClientOptions options() {
        return options;
    }
    
}
