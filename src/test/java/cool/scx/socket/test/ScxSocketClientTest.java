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

        //支持未连接时发送
        scxSocketClient.sendEvent("a", new User("jack", 24));
        scxSocketClient.sendEvent("ss", List.of(new User("jack", 24)), (s) -> {
            System.out.println("服务端的响应 " + s);
        }, new TypeReference<List<User>>() {});

        scxSocketClient.send("abc");

        scxSocketClient.onOpen((v) -> {
            System.out.println("onOpen");
        });

        scxSocketClient.onEvent("b", m -> {
            System.out.println("服务端发送的消息 : " + m);
        }, User.class);

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            scxSocketClient.sendEvent("aaa", i, (d) -> {
                System.out.println(d + "  " + finalI);
            });
        }

        scxSocketClient.connect();

    }

}
