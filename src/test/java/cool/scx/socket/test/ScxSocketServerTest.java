package cool.scx.socket.test;

import cool.scx.http.x.HttpServer;
import cool.scx.http.x.HttpServerOptions;
import cool.scx.reflect.TypeReference;
import cool.scx.socket.ScxSocketServer;
import cool.scx.websocket.ScxServerWebSocketHandshakeRequest;
import cool.scx.websocket.x.WebSocketUpgradeHandler;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class ScxSocketServerTest extends InitLogger {

    public static void main(String[] args) throws IOException {
        test1();
    }

    @Test
    public static void test1() throws IOException {
        //创建服务器
        var scxSocketServer = new ScxSocketServer();

        scxSocketServer.onConnect(clientContent -> {

            clientContent.onMessage((m) -> {
                System.out.println("客户端发来的消息 : " + m);
            });

            clientContent.onEvent("a", (r) -> {
                var m = r.payload(new TypeReference<User>() {});
                System.out.println("客户端发来的事件 : " + m);
                clientContent.sendEvent("b", m);
            });

            clientContent.onClose((i, s) -> {
                System.out.println("close");
            });

            clientContent.onError(e -> {
                e.printStackTrace();
            });

            //响应方法 
            clientContent.onEvent("aaa", (r) -> {
                r.response(r.payload() + "🙄");
            });

            //相同事件名称 会覆盖
            clientContent.onEvent("aaa", (r) -> {
                r.response(r.payload() + "😆");
            });

            clientContent.onEvent("ss", (r) -> {
                var c = r.payload(new TypeReference<List<User>>() {});
                c.add(new User("Tom", 88));
                r.response(c);
            });

        });

//        //使用 httpServer
//        new HelidonHttpServer(new HelidonHttpServerOptions().port(8990))
//                .onWebSocket(scxSocketServer::call)
//                .start();

        //使用 httpServer
        new HttpServer(new HttpServerOptions().addUpgradeHandler(new WebSocketUpgradeHandler()))
                .onRequest(c -> {
                    if (c instanceof ScxServerWebSocketHandshakeRequest s) {
                        scxSocketServer.call(s);
                    }
                })
                .start(8990);

    }

}
