package cool.scx.socket;

import io.vertx.core.http.WebSocket;

/**
 * 客户端 Socket 对象
 */
public final class ScxClientSocket extends PingPongManager {

    private final ScxSocketClient socketClient;

    ScxClientSocket(WebSocket webSocket, String clientID, ScxSocketClient socketClient) {
        super(webSocket, clientID, socketClient.options);
        this.socketClient = socketClient;
    }

    ScxClientSocket(WebSocket webSocket, String clientID, ScxSocketClient socketClient, ScxSocketStatus status) {
        super(webSocket, clientID, socketClient.options, status);
        this.socketClient = socketClient;
    }

    @Override
    protected void doClose(Void unused) {
        super.doClose(unused);
        this.socketClient.connect();
    }

    @Override
    protected void doError(Throwable e) {
        super.doError(e);
        this.socketClient.connect();
    }

    @Override
    public void close() {
        this.socketClient.removeConnectFuture();
        this.socketClient.cancelReconnect();
        this.resetCloseOrErrorBind();
        super.close();
    }

    /**
     * 重置 关闭和 错误的 handler
     */
    private void resetCloseOrErrorBind() {
        if (!this.webSocket.isClosed()) {
            this.webSocket.closeHandler(null);
            this.webSocket.exceptionHandler(null);
        }
    }

    @Override
    protected void doPingTimeout() {
        //心跳失败直接重连
        this.socketClient.connect();
    }

}
