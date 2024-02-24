package cool.scx.socket;

import io.netty.util.Timeout;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketConnectOptions;

import java.util.function.Consumer;

import static cool.scx.socket.ScxSocketHelper.initConnectOptions;
import static cool.scx.socket.ScxSocketHelper.setTimeout;
import static cool.scx.util.RandomUtils.randomUUID;
import static java.lang.System.Logger.Level.DEBUG;

public final class ScxSocketClient extends TypeConverter {

    private final WebSocketConnectOptions connectOptions;
    private final WebSocketClient webSocketClient;
    private final ScxSocketClientOptions clientOptions;
    private Timeout reconnectTimeout;
    private SingleListenerFuture<WebSocket> connectFuture;
    private Consumer<Void> onOpen;

    public ScxSocketClient(String uri, WebSocketClient webSocketClient, String clientID, ScxSocketClientOptions clientOptions) {
        super(clientOptions, clientID);
        this.clientOptions = clientOptions;
        this.webSocketClient = webSocketClient;
        this.connectOptions = initConnectOptions(uri, this.clientID);
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

    private void removeConnectFuture() {
        if (this.connectFuture != null) {
            this.connectFuture.onSuccess(WebSocketBase::close).onFailure(null);
            this.connectFuture = null;
        }
    }

    public void onOpen(Consumer<Void> onOpen) {
        this.onOpen = onOpen;
    }

    private void cancelReconnect() {
        if (this.reconnectTimeout != null) {
            this.reconnectTimeout.cancel();
            this.reconnectTimeout = null;
        }
    }

    public void connect() {
        //当前已经存在一个连接中的任务
        if (this.connectFuture != null && !this.connectFuture.isComplete()) {
            return;
        }
        //关闭上一次连接
        this.close();
        this.connectFuture = new SingleListenerFuture<>(webSocketClient.connect(connectOptions));
        this.connectFuture.onSuccess((webSocket) -> {
            this.start(webSocket);
            this.doOpen();
        }).onFailure((v) -> this.reconnect());
    }

    private void doOpen() {
        callOnOpen(null);
    }

    @Override
    protected void doClose(Void unused) {
        super.doClose(unused);
        this.connect();
    }

    @Override
    protected void doError(Throwable e) {
        super.doError(e);
        this.connect();
    }

    private void reconnect() {
        //如果当前已经存在一个重连进程 则不进行重连
        if (this.reconnectTimeout != null) {
            return;
        }
        logger.log(DEBUG, "WebSocket 重连中... ");
        this.reconnectTimeout = setTimeout(() -> {  //没连接上会一直重连，设置延迟为5000毫秒避免请求过多
            this.reconnectTimeout = null;
            this.connect();
        }, clientOptions.getReconnectTimeout());
    }

    @Override
    public void close() {
        removeConnectFuture();
        cancelReconnect();
        resetCloseOrErrorBind();
        super.close();
    }

    /**
     * 重置 关闭和 错误的 handler
     */
    private void resetCloseOrErrorBind() {
        if (this.webSocket != null && !this.webSocket.isClosed()) {
            this.webSocket.closeHandler(null);
            this.webSocket.exceptionHandler(null);
        }
    }

    @Override
    protected void doPingTimeout() {
        //心跳失败直接重连
        this.connect();
    }

    private void callOnOpen(Void v) {
        if (this.onOpen != null) {
            this.onOpen.accept(v);
        }
    }

    private void callOnOpenAsync(Void v) {
        if (this.onOpen != null) {
            Thread.ofVirtual().start(() -> this.onOpen.accept(v));
        }
    }

}
