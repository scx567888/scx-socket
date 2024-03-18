package cool.scx.socket;

import io.vertx.core.http.ServerWebSocket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static cool.scx.socket.helper.Helper.getClientID;

public final class ScxSocketServer {

    final ConcurrentMap<String, ScxServerSocket> map;
    final ScxSocketServerOptions options;
    private Consumer<ScxServerSocket> onConnect;

    public ScxSocketServer() {
        this(new ScxSocketServerOptions());
    }

    public ScxSocketServer(ScxSocketServerOptions options) {
        this.options = options;
        this.map = new ConcurrentHashMap<>();
    }

    public void onConnect(Consumer<ScxServerSocket> onConnect) {
        this.onConnect = onConnect;
    }

    public ScxServerSocket getClient(String clientID) {
        return map.get(clientID);
    }

    public ScxServerSocket getOrCreateClient(String clientID) {
        return map.computeIfAbsent(clientID, (k) -> new ScxServerSocket(null, clientID, this));
    }

    public Collection<ScxServerSocket> getClients() {
        return map.values();
    }

    public void call(ServerWebSocket serverWebSocket) {
        var clientID = getClientID(serverWebSocket);
        if (clientID == null) {
            serverWebSocket.reject(400);
        }

        var newClientConnect = map.compute(clientID, (k, oldClientConnect) -> {
            if (oldClientConnect == null) {
                return new ScxServerSocket(serverWebSocket, clientID, this);
            } else {
                //关闭旧连接 同时 将一些数据 存到 新的中
                oldClientConnect.close();
                return new ScxServerSocket(serverWebSocket, clientID, this, oldClientConnect.status);
            }
        });
        newClientConnect.start();
        _callOnConnect(newClientConnect);
    }

    private void _callOnConnect(ScxServerSocket clientConnect) {
        if (this.onConnect != null) {
            this.onConnect.accept(clientConnect);
        }
    }

}
