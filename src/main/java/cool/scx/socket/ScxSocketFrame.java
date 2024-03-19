package cool.scx.socket;

/**
 * ScxSocket 帧结构
 */
public final class ScxSocketFrame {

    /**
     * 序列 ID
     */
    public long seq_id;

    /**
     * 类型  参见 {@link Type}
     */
    public byte type;

    /**
     * 时间戳
     */
    public long now;

    /**
     * 事件名称
     */
    public String event_name;

    /**
     * 有效载荷
     */
    public String payload;

    /**
     * 应答 ID
     */
    public long ack_id;

    /**
     * 是否需要 应答
     */
    public boolean need_ack;

    /**
     * 是否需要 响应
     */
    public boolean need_response;

    /**
     * 从 JSON 反序列化
     *
     * @param json json
     * @return a
     */
    public static ScxSocketFrame fromJson(String json) {
        return Helper.fromJson(json, ScxSocketFrame.class);
    }

    /**
     * 序列化到 JSON
     *
     * @return json
     */
    public String toJson() {
        return Helper.toJson(this);
    }

    /**
     * 类型定义
     */
    public static class Type {

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

}
