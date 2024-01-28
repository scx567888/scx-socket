package cool.scx.socket;

import io.netty.util.Timeout;
import io.vertx.core.http.WebSocketBase;

import static cool.scx.socket.ScxSocketHelper.setTimeout;
import static java.lang.System.Logger.Level.DEBUG;

/**
 * 客户端连接对象
 */
public final class ScxSocketClientConnect extends TypeConverter {

    private final ScxSocketServer scxSocketServer;
    private final ScxSocketServerOptions serverOptions;
    private Timeout removeClosedClientTimeout;

    public ScxSocketClientConnect(String clientID, ScxSocketServerOptions serverOptions, ScxSocketServer scxSocketServer) {
        super(serverOptions,clientID);
        this.serverOptions = serverOptions;
        this.scxSocketServer = scxSocketServer;
    }

    @Override
    void start(WebSocketBase webSocket) {
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
            logger.log(DEBUG, "客户端超时未连接 已移除 , clientID : {0}", clientID);
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
