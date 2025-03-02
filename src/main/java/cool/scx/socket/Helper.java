package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.scx.common.util.ObjectUtils;
import cool.scx.http.uri.ScxURI;
import cool.scx.http.uri.ScxURIWritable;
import cool.scx.http.web_socket.ScxServerWebSocket;

import static cool.scx.common.exception.ScxExceptionHelper.wrap;


/**
 * Helper
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class Helper {

    public static final String SCX_SOCKET_CLIENT_ID = "scx-socket-client-id";

    private static final ObjectMapper JSON_MAPPER = ObjectUtils.jsonMapper();

    /**
     * 从 ServerWebSocket 中获取 clientID
     *
     * @param serverWebSocket serverWebSocket
     * @return clientID 没有返回 null
     */
    public static String getClientID(ScxServerWebSocket serverWebSocket) {
        return serverWebSocket.uri().getQuery(SCX_SOCKET_CLIENT_ID);
    }

    /**
     * 根据 uri 和 clientID 创建 ConnectOptions
     *
     * @param absoluteURI 后台连接的绝对路径
     * @param clientID    客户端 ID
     * @return ConnectOptions
     */
    public static ScxURIWritable createConnectOptions(String absoluteURI, String clientID) {
        return ScxURI.of(absoluteURI).setQuery(SCX_SOCKET_CLIENT_ID, clientID);
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

    public static String toJson(Object data) {
        return wrap(() -> JSON_MAPPER.writeValueAsString(data));
    }

    public static <T> T fromJson(String json, Class<T> tClass) {
        return json == null ? null : wrap(() -> JSON_MAPPER.readValue(json, tClass));
    }

    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        return json == null ? null : wrap(() -> JSON_MAPPER.readValue(json, valueTypeRef));
    }

}
