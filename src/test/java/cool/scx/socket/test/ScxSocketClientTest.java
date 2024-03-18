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
        ScxSocketServerTest.test1();

        var webSocketClient = VERTX.createWebSocketClient();

        var scxSocketClient = new ScxSocketClient("ws://127.0.0.1:8990/test", webSocketClient);

        scxSocketClient.onConnect(c -> {
            System.out.println("onOpen");
            //支持未连接时发送
            c.sendEvent("a", new User("jack", 24));
            c.sendEvent("ss", List.of(new User("jack", 24)), (s, e) -> {
                System.out.println("服务端的响应 " + s);
            }, new TypeReference<List<User>>() {});

            c.send("abc");

            c.onEvent("b", m -> {
                System.out.println("服务端发送的消息 : " + m);
            }, User.class);

            for (int i = 0; i < 10; i++) {
                int finalI = i;
                c.sendEvent("aaa", i, (d, e) -> {
                    System.out.println(d + "  " + finalI);
                });
            }
        });

        scxSocketClient.connect();

    }

}
