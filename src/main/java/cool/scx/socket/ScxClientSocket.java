package cool.scx.socket;

import cool.scx.socket.core.ScxSocket;
import cool.scx.socket.core.ScxSocketStatus;
import cool.scx.socket.frame.ScxSocketFrame;
import cool.scx.socket.ping_pong.PingPongManager;
import io.vertx.core.http.WebSocket;

import static cool.scx.socket.frame.ScxSocketFrame.Type.PING;
import static cool.scx.socket.frame.ScxSocketFrame.Type.PONG;

/**
 * 客户端 Socket 对象
 */
public final class ScxClientSocket extends ScxSocket {

    private final ScxSocketClient socketClient;
    private final PingPongManager pingPongManager;

    public ScxClientSocket(WebSocket webSocket, String clientID, ScxSocketClient socketClient) {
        super(webSocket, clientID, socketClient.options());
        this.socketClient = socketClient;
        //心跳失败直接重连
        this.pingPongManager = new PingPongManager(this.socketClient::connect, this, socketClient.options());
    }

    public ScxClientSocket(WebSocket webSocket, String clientID, ScxSocketClient socketClient, ScxSocketStatus status) {
        super(webSocket, clientID, socketClient.options(), status);
        this.socketClient = socketClient;
        //心跳失败直接重连
        this.pingPongManager = new PingPongManager(this.socketClient::connect, this, socketClient.options());
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
    protected void start() {
        super.start();
        //启动心跳
        this.pingPongManager.startPing();
        //心跳超时
        this.pingPongManager.startPingTimeout();
    }

    @Override
    public void close() {
        this.socketClient.removeConnectFuture();
        this.socketClient.cancelReconnect();
        this.resetCloseOrErrorBind();
        //取消心跳
        this.pingPongManager.cancelPing();
        //取消心跳超时
        this.pingPongManager.cancelPingTimeout();
        super.close();
    }

    @Override
    protected void doSocketFrame(ScxSocketFrame socketFrame) {
        //只要收到任何消息就重置 心跳 
        this.pingPongManager.startPing();
        this.pingPongManager.startPingTimeout();
        switch (socketFrame.type) {
            case PING -> this.pingPongManager.doPing(socketFrame);
            case PONG -> this.pingPongManager.doPong(socketFrame);
            default -> super.doSocketFrame(socketFrame);
        }
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

}
