package cool.scx.socket.test;

import cool.scx.reflect.TypeReference;
import cool.scx.socket.ScxSocketClient;
import cool.scx.websocket.x.WebSocketClient;

import java.io.IOException;
import java.util.List;

public class ScxSocketClientTest extends InitLogger {

    public static void main(String[] args) throws IOException {
        test1();
    }

//    @Test
    public static void test1() throws IOException {
        //启动服务器
        ScxSocketServerTest.test1();

        var webSocketClient = new WebSocketClient();

        var scxSocketClient = new ScxSocketClient("ws://127.0.0.1:8990/test", webSocketClient);

        scxSocketClient.onConnect(c -> {

            System.out.println("onOpen");

            c.sendEvent("a", new User("jack", 24));
            c.sendEvent("ss", List.of(new User("jack", 24)), r -> {
                if (r.isSuccess()) {
                    var s = r.payload(new TypeReference<List<User>>() {});
                    System.out.println("服务端的响应 " + s);
                } else {
                    System.out.println("服务端的响应超时 ");
                }
            });

            c.send("abc");

            c.onEvent("b", r -> {
                var m = r.payload(new TypeReference<User>() {});
                System.out.println("服务端发送的消息 : " + m);
            });

            for (int i = 0; i < 100000; i = i + 1) {
                int finalI = i;
                c.sendEvent("aaa", i, r -> {
                    System.out.println(r.payload() + "  " + finalI);
                });
            }

        });

        scxSocketClient.connect();

    }

}
