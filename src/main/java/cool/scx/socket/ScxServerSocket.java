package cool.scx.socket;

import cool.scx.http.web_socket.ScxServerWebSocket;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.DEBUG;


/**
 * ScxServerSocket
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class ScxServerSocket extends PingPongManager {

    private final ScxSocketServer scxSocketServer;
    private ScheduledFuture<?> removeClosedClientTimeout;

    ScxServerSocket(ScxServerWebSocket serverWebSocket, String clientID, ScxSocketServer scxSocketServer) {
        super(serverWebSocket, clientID, scxSocketServer.options);
        this.scxSocketServer = scxSocketServer;
    }

    ScxServerSocket(ScxServerWebSocket serverWebSocket, String clientID, ScxSocketServer scxSocketServer, ScxSocketStatus status) {
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
        this.removeClosedClientTimeout = scheduledExecutor.schedule(this::removeClosedClient, scxSocketServer.options.getStatusKeepTime(), TimeUnit.MILLISECONDS);
    }

    private void cancelRemoveClosedClientTask() {
        if (this.removeClosedClientTimeout != null) {
            this.removeClosedClientTimeout.cancel(false);
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
