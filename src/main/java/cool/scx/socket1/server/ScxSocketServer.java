package cool.scx.socket1.server;

import cool.scx.socket1.client.ScxSocketClientConnect;
import io.vertx.core.http.ServerWebSocket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static cool.scx.socket1.helper.Helper.getClientID;

public final class ScxSocketServer {

    final ConcurrentMap<String, ScxSocketClientConnect> clientConnectMap;
    final ScxSocketServerOptions options;
    private Consumer<ScxSocketClientConnect> onClientConnect;

    public ScxSocketServer() {
        this(new ScxSocketServerOptions());
    }

    public ScxSocketServer(ScxSocketServerOptions options) {
        this.options = options;
        this.clientConnectMap = new ConcurrentHashMap<>();
    }

    public void onClientConnect(Consumer<ScxSocketClientConnect> onClientConnect) {
        this.onClientConnect = onClientConnect;
    }

    public ScxSocketClientConnect getClient(String clientID) {
        return clientConnectMap.get(clientID);
    }

    public ScxSocketClientConnect getOrCreateClient(String clientID) {
        return clientConnectMap.computeIfAbsent(clientID, (k) -> new ScxSocketClientConnect(clientID, options, this));
    }

    public Collection<ScxSocketClientConnect> getClients() {
        return clientConnectMap.values();
    }

    public void call(ServerWebSocket serverWebSocket) {
        var clientID = getClientID(serverWebSocket);
        if (clientID == null) {
            serverWebSocket.reject(400);
        }

        var newClientConnect = clientConnectMap.compute(clientID, (k, oldClientConnect) -> {
            if (oldClientConnect == null) {
                return new ScxSocketClientConnect(clientID, options, this);
            } else {
                //关闭旧连接 同时 将一些数据 存到 新的中
                oldClientConnect.close();
                return new ScxSocketClientConnect(oldClientConnect);
            }
        });

        newClientConnect.start(serverWebSocket);
        callOnClientConnectAsync(newClientConnect);
    }

    private void callOnClientConnect(ScxSocketClientConnect clientConnect) {
        if (this.onClientConnect != null) {
            this.onClientConnect.accept(clientConnect);
        }
    }

    private void callOnClientConnectAsync(ScxSocketClientConnect clientConnect) {
        if (this.onClientConnect != null) {
            Thread.ofVirtual().start(() -> this.onClientConnect.accept(clientConnect));
        }
    }

}
