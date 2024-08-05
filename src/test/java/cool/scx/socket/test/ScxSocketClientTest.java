package cool.scx.socket.test;

import com.fasterxml.jackson.core.type.TypeReference;
import cool.scx.socket.ScxSocketClient;
import org.testng.annotations.Test;

import java.util.List;

import static cool.scx.socket.test.ScxSocketServerTest.VERTX;

public class ScxSocketClientTest extends InitLogger {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        //启动服务器
//        ScxSocketServerTest.test1();

        var webSocketClient = VERTX.createWebSocketClient();

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

            for (int i = 0; i < 10; i++) {
                int finalI = i;
                c.sendEvent("aaa", i, r -> {
                    System.out.println(r.payload() + "  " + finalI);
                });
            }
        });

        scxSocketClient.connect();

    }

}
