package cool.scx.socket;

import cool.scx.socket.ping_pong.PingPongManager;
import io.netty.util.Timeout;
import io.vertx.core.http.WebSocketBase;

import static cool.scx.socket.helper.Helper.setTimeout;
import static java.lang.System.Logger.Level.DEBUG;

/**
 * 客户端连接对象
 */
public final class ScxSocketClientConnect extends PingPongManager {

    private final ScxSocketServer scxSocketServer;
    private final ScxSocketServerOptions serverOptions;
    private Timeout removeClosedClientTimeout;

    public ScxSocketClientConnect(String clientID, ScxSocketServerOptions serverOptions, ScxSocketServer scxSocketServer) {
        super(serverOptions, clientID);
        this.serverOptions = serverOptions;
        this.scxSocketServer = scxSocketServer;
    }

    public ScxSocketClientConnect(ScxSocketClientConnect oldClientConnect) {
        super(oldClientConnect);
        this.serverOptions = oldClientConnect.serverOptions;
        this.scxSocketServer = oldClientConnect.scxSocketServer;
    }

    @Override
    protected void start(WebSocketBase webSocket) {
        super.start(webSocket);
        cancelRemoveClosedClientTask();
    }

    @Override
    public void close() {
        super.close();
        startRemoveClosedClientTask();
    }

    private void startRemoveClosedClientTask() {
        cancelRemoveClosedClientTask();
        this.removeClosedClientTimeout = setTimeout(this::removeClosedClient, serverOptions.getRemoveClosedClientTimeout());
    }

    private void cancelRemoveClosedClientTask() {
        if (this.removeClosedClientTimeout != null) {
            this.removeClosedClientTimeout.cancel();
            this.removeClosedClientTimeout = null;
        }
    }

    private void removeClosedClient() {
        this.scxSocketServer.clientConnectMap.remove(this.clientID);

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 客户端超时未连接 已移除", clientID);
        }

    }

    /**
     * 服务端不需要 ping 客户端 只需要等待 pong 并在心跳超时后关闭即可
     */
    @Override
    protected void startPing() {
        //什么也不需要做
    }

    @Override
    protected void doPingTimeout() {
        this.close();
    }

}
