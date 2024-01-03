package cool.scx.socket.test;

import com.fasterxml.jackson.core.type.TypeReference;
import cool.scx.socket.ScxSocketServer;
import io.vertx.core.Vertx;
import org.testng.annotations.Test;

import java.util.List;

public class ScxSocketServerTest extends InitLogger {
    public static final Vertx VERTX = Vertx.vertx();

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        //创建服务器
        var scxSocketServer = new ScxSocketServer();

        scxSocketServer.onClientConnect(clientContent -> {

            clientContent.onMessage((m) -> {
                System.out.println("客户端发来的消息 : " + m);
            });

            clientContent.onEvent("a", (m) -> {
                System.out.println("客户端发来的事件 : " + m);
                clientContent.sendEvent("b", m);
            }, User.class);

            clientContent.onClose(c -> {
                System.out.println("close");
            });

            clientContent.onError(e -> {
                e.printStackTrace();
            });

            //响应方法一 直接返回 
            clientContent.onEvent("aaa", (c) -> {
                return c + "🙄";
            });

            //相同事件名称 会覆盖
            //响应方法二 通过第二个 request 参数 进行回调
            clientContent.onEvent("aaa", (c, request) -> {
                request.response(c + "😆");
            });

            clientContent.onEvent("aaa", (c, request) -> {
                request.response(c + "😆");
            });

            clientContent.onEvent("ss", (c, request) -> {
                c.add(new User("Tom", 88));
                request.response(c);
            }, new TypeReference<List<User>>() {});

        });

        //使用 vertx 的 httpServer 
        VERTX.createHttpServer()
                .webSocketHandler(scxSocketServer::call)
                .listen(8990);

    }

}
