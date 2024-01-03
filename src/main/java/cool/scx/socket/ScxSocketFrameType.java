package cool.scx.socket;

public class ScxSocketFrameType {

    /**
     * 消息
     */
    public static final byte MESSAGE = 0;

    /**
     * 响应
     */
    public static final byte RESPONSE = 1;

    /**
     * ACK
     */
    public static final byte ACK = 2;

    /**
     * 心跳 ping
     */
    public static final byte PING = 3;

    /**
     * 心跳 pong
     */
    public static final byte PONG = 4;

}
