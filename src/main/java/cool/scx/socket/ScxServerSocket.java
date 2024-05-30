package cool.scx.socket;

import io.netty.util.Timeout;
import io.vertx.core.http.ServerWebSocket;

import static cool.scx.socket.Helper.setTimeout;
import static java.lang.System.Logger.Level.DEBUG;

public final class ScxServerSocket extends PingPongManager {

    private final ScxSocketServer scxSocketServer;
    private Timeout removeClosedClientTimeout;

    ScxServerSocket(ServerWebSocket serverWebSocket, String clientID, ScxSocketServer scxSocketServer) {
        super(serverWebSocket, clientID, scxSocketServer.options);
        this.scxSocketServer = scxSocketServer;
    }

    ScxServerSocket(ServerWebSocket serverWebSocket, String clientID, ScxSocketServer scxSocketServer, ScxSocketStatus status) {
        super(serverWebSocket, clientID, scxSocketServer.options, status);
        this.scxSocketServer = scxSocketServer;
    }

    @Override
    protected void start() {
        super.start();
        this.cancelRemoveClosedClientTask();
    }

    @Override
    public void close() {
        this.startRemoveClosedClientTask();
        super.close();
    }

    private void startRemoveClosedClientTask() {
        cancelRemoveClosedClientTask();
        this.removeClosedClientTimeout = setTimeout(this::removeClosedClient, scxSocketServer.options.getStatusKeepTime());
    }

    private void cancelRemoveClosedClientTask() {
        if (this.removeClosedClientTimeout != null) {
            this.removeClosedClientTimeout.cancel();
            this.removeClosedClientTimeout = null;
        }
    }

    private void removeClosedClient() {
        this.scxSocketServer.serverSockets.remove(this.clientID);

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 客户端超时未连接 已移除", this.clientID);
        }

    }

    @Override
    protected void startPing() {
        //服务端无需 ping 客户端
    }

    @Override
    protected void doPingTimeout() {
        //心跳失败直接关闭
        this.close();
    }

}
