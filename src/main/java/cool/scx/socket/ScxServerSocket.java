package cool.scx.socket;

import cool.scx.socket.core.ScxSocket;
import cool.scx.socket.core.ScxSocketStatus;
import cool.scx.socket.frame.ScxSocketFrame;
import cool.scx.socket.ping_pong.PingPongManager;
import io.netty.util.Timeout;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketBase;

import static cool.scx.socket.frame.ScxSocketFrame.Type.PING;
import static cool.scx.socket.frame.ScxSocketFrame.Type.PONG;
import static cool.scx.socket.helper.Helper.setTimeout;
import static java.lang.System.Logger.Level.DEBUG;

public class ScxServerSocket extends ScxSocket {

    private final ScxSocketServer scxSocketServer;
    private Timeout removeClosedClientTimeout;
    private final PingPongManager pingPongManager;

    public ScxServerSocket(ServerWebSocket serverWebSocket, String clientID, ScxSocketServer scxSocketServer) {
        super(serverWebSocket, clientID, scxSocketServer.options);
        this.scxSocketServer = scxSocketServer;
        //心跳失败直接关闭
        this.pingPongManager = new PingPongManager(this::close, this, scxSocketServer.options);
    }

    public ScxServerSocket(ServerWebSocket serverWebSocket, String clientID, ScxSocketServer scxSocketServer, ScxSocketStatus status) {
        super(serverWebSocket, clientID, scxSocketServer.options, status);
        this.scxSocketServer = scxSocketServer;
        //心跳失败直接关闭
        this.pingPongManager = new PingPongManager(this::close, this, scxSocketServer.options);
    }

    @Override
    protected void start() {
        super.start();
        this.cancelRemoveClosedClientTask();
        //心跳超时
        this.pingPongManager.startPingTimeout();
    }

    @Override
    public void close() {
        this.startRemoveClosedClientTask();
        //取消心跳超时
        this.pingPongManager.cancelPingTimeout();
        super.close();
    }

    @Override
    protected void doSocketFrame(ScxSocketFrame socketFrame) {
        //只要收到任何消息就重置 心跳
        this.pingPongManager.startPingTimeout();
        switch (socketFrame.type) {
            case PING -> this.pingPongManager.doPing(socketFrame);
            case PONG -> this.pingPongManager.doPong(socketFrame);
            default -> super.doSocketFrame(socketFrame);
        }
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
        this.scxSocketServer.map.remove(this.clientID);

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 客户端超时未连接 已移除", clientID);
        }

    }

}
