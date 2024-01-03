package cool.scx.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.scx.util.ObjectUtils;
import cool.scx.util.URIBuilder;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketConnectOptions;

import java.util.concurrent.TimeUnit;

final class ScxSocketHelper {

    public static final String SCX_SOCKET_CLIENT_ID_KEY = "scx-socket-client-id";

    static final ObjectMapper JSON_MAPPER = ObjectUtils.jsonMapper();

    private static final HashedWheelTimer HASHED_WHEEL_TIMER = new HashedWheelTimer(Thread.ofVirtual().factory());

    public static Timeout setTimeout(Runnable task, long delay) {
        return HASHED_WHEEL_TIMER.newTimeout((v) -> task.run(), delay, TimeUnit.MILLISECONDS);
    }

    public static String getClientID(ServerWebSocket serverWebSocket) {
        var decoder = new QueryStringDecoder(serverWebSocket.uri());
        var parameters = decoder.parameters();
        var clientIDValues = parameters.get(SCX_SOCKET_CLIENT_ID_KEY);
        return clientIDValues.isEmpty() ? null : clientIDValues.get(0);
    }

    public static WebSocketConnectOptions initConnectOptions(String uri, String clientID) {
        var o = new WebSocketConnectOptions().setAbsoluteURI(uri);
        var oldUri = o.getURI();
        var newUri = URIBuilder.of(oldUri).addParam(SCX_SOCKET_CLIENT_ID_KEY, clientID).toString();
        o.setURI(newUri);
        return o;
    }

    /**
     * 根据次数获取延时时间
     * 根据次数进行 2的 次方倍增 , 如 1, 2 ,4 ,8 ,16 等
     *
     * @param times 次数 (0 起始)
     * @return 延时时间 (毫秒)
     */
    public static long getDelayed(int times) {
        return 1000L * (1L << times);
    }

}
