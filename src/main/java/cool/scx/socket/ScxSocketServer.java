package cool.scx.socket;

import io.vertx.core.http.ServerWebSocket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static cool.scx.socket.Helper.getClientID;

public final class ScxSocketServer {

    final ConcurrentMap<String, ScxServerSocket> serverSockets;
    final ScxSocketServerOptions options;
    private Consumer<ScxServerSocket> onConnect;

    public ScxSocketServer() {
        this(new ScxSocketServerOptions());
    }

    public ScxSocketServer(ScxSocketServerOptions options) {
        this.options = options;
        this.serverSockets = new ConcurrentHashMap<>();
    }

    public void onConnect(Consumer<ScxServerSocket> onConnect) {
        this.onConnect = onConnect;
    }

    private void _callOnConnect(ScxServerSocket serverSocket) {
        if (this.onConnect != null) {
            this.onConnect.accept(serverSocket);
        }
    }

    public void call(ServerWebSocket serverWebSocket) {
        var clientID = getClientID(serverWebSocket);
        if (clientID == null) {
            serverWebSocket.reject(400);
        }

        var serverSocket = serverSockets.compute(clientID, (k, old) -> {
            if (old == null) {
                return new ScxServerSocket(serverWebSocket, clientID, this);
            } else {
                //关闭旧连接 同时 将一些数据 存到 新的中
                old.close();
                return new ScxServerSocket(serverWebSocket, clientID, this, old.status);
            }
        });

        serverSocket.start();
        _callOnConnect(serverSocket);
    }

    public ScxServerSocket getServerSocket(String clientID) {
        return serverSockets.get(clientID);
    }

    public Collection<ScxServerSocket> getServerSockets() {
        return serverSockets.values();
    }

    public static final class ScxSocketServerOptions extends PingPongOptions {

        private int statusKeepTime;

        public ScxSocketServerOptions() {
            this.statusKeepTime = 1000 * 60 * 30; // 默认 30 分钟
        }

        public int getStatusKeepTime() {
            return statusKeepTime;
        }

        public ScxSocketServerOptions setStatusKeepTime(int statusKeepTime) {
            this.statusKeepTime = statusKeepTime;
            return this;
        }

    }

}
