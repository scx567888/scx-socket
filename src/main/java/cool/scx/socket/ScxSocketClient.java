package cool.scx.socket;

import cool.scx.http.ScxHttpClient;
import cool.scx.http.uri.ScxURIWritable;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static cool.scx.common.util.RandomUtils.randomUUID;
import static cool.scx.socket.Helper.createConnectOptions;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;


/**
 * ScxSocketClient
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class ScxSocketClient {

    private static final System.Logger logger = getLogger(ScxSocketClient.class.getName());

    final ScxURIWritable connectOptions;
    final ScxHttpClient webSocketClient;
    final String clientID;
    final ScxSocketClientOptions options;
    final ScheduledExecutorService scheduledExecutor;
    final Executor executor;

    private ScxClientSocket clientSocket;
    private Consumer<ScxClientSocket> onConnect;
    private ScheduledFuture<?> reconnectTimeout;

    public ScxSocketClient(String uri, ScxHttpClient webSocketClient, String clientID, ScxSocketClientOptions options) {
        this.connectOptions = createConnectOptions(uri, clientID);
        this.webSocketClient = webSocketClient;
        this.clientID = clientID;
        this.options = options;
        this.scheduledExecutor = options.scheduledExecutor();
        this.executor = options.executor();
    }

    public ScxSocketClient(String uri, ScxHttpClient webSocketClient, ScxSocketClientOptions options) {
        this(uri, webSocketClient, randomUUID(), options);
    }

    public ScxSocketClient(String uri, ScxHttpClient webSocketClient, String clientID) {
        this(uri, webSocketClient, clientID, new ScxSocketClientOptions());
    }

    public ScxSocketClient(String uri, ScxHttpClient webSocketClient) {
        this(uri, webSocketClient, randomUUID(), new ScxSocketClientOptions());
    }

    public void onConnect(Consumer<ScxClientSocket> onConnect) {
        this.onConnect = onConnect;
    }

    private void _callOnConnect(ScxClientSocket clientSocket) {
        if (this.onConnect != null) {
            //为了防止用户回调 将线程卡死 这里独立创建一个线程处理
            executor.execute(() -> this.onConnect.accept(clientSocket));
        }
    }

    public void connect() {
        //当前已经存在一个连接中的任务
        //todo 处理多次连接的问题
//        if (this.connectFuture != null && !this.connectFuture.isComplete()) {
//            return;
//        }
        //关闭上一次连接
        this._closeOldSocket();
        //创建连接

        try {
            webSocketClient.webSocketHandshakeRequest()
                    .uri(connectOptions)
                    .onWebSocket(webSocket -> {
                        //如果存在旧的 则使用旧的 status
                        this.clientSocket = clientSocket != null ?
                                new ScxClientSocket(webSocket, clientID, this, clientSocket.status) :
                                new ScxClientSocket(webSocket, clientID, this);

                        this.clientSocket.start();
                        this._callOnConnect(clientSocket);
                    });
        } catch (Exception e) {
            this.reconnect(e);
        }
    }

    void reconnect(Throwable e) {
        //如果当前已经存在一个重连进程 则不进行重连
        if (this.reconnectTimeout != null) {
            return;
        }
        logger.log(DEBUG, "WebSocket 重连中... CLIENT_ID : {0}", clientID, e);
        this.reconnectTimeout = scheduledExecutor.schedule(() -> {  //没连接上会一直重连，设置延迟为5000毫秒避免请求过多
            this.reconnectTimeout = null;
            this.connect();
        }, options.getReconnectTimeout(), TimeUnit.MILLISECONDS);
    }

    void cancelReconnect() {
        if (this.reconnectTimeout != null) {
            this.reconnectTimeout.cancel(false);
            this.reconnectTimeout = null;
        }
    }

    void removeConnectFuture() {
//        if (this.connectFuture != null) {
//            //只有当未完成的时候才设置
//            if (!this.connectFuture.isComplete()) {
//                this.connectFuture.onSuccess(WebSocketBase::close).onFailure(null);
//            }
//            this.connectFuture = null;
//        }
    }

    private void _closeOldSocket() {
        if (this.clientSocket != null) {
            this.clientSocket.close();
        }
    }

}
