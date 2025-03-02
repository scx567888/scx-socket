package cool.scx.socket;

import cool.scx.http.web_socket.ScxServerWebSocket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static cool.scx.socket.Helper.getClientID;


/**
 * ScxSocketServer
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class ScxSocketServer {

    final ConcurrentMap<String, ScxServerSocket> serverSockets;
    final ScxSocketServerOptions options;
    final Executor executor;
    private Consumer<ScxServerSocket> onConnect;

    public ScxSocketServer() {
        this(new ScxSocketServerOptions());
    }

    public ScxSocketServer(ScxSocketServerOptions options) {
        this.options = options;
        this.serverSockets = new ConcurrentHashMap<>();
        this.executor = options.executor();
    }

    public void onConnect(Consumer<ScxServerSocket> onConnect) {
        this.onConnect = onConnect;
    }

    private void _callOnConnect(ScxServerSocket serverSocket) {
        if (this.onConnect != null) {
            executor.execute(() -> this.onConnect.accept(serverSocket));
        }
    }

    public void call(ScxServerWebSocket serverWebSocket) {
        var clientID = getClientID(serverWebSocket);
        if (clientID == null) {
            //todo 如何拒绝连接
            serverWebSocket.close();
//            serverWebSocket.close(400);
            return;
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

}
