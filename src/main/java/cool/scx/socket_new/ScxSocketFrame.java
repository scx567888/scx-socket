package cool.scx.socket_new;

import com.fasterxml.jackson.core.JsonProcessingException;
import cool.scx.util.ScxExceptionHelper.ScxWrappedRuntimeException;

import static cool.scx.socket_new.ScxSocketHelper.JSON_MAPPER;

/**
 * ScxSocket 帧结构
 */
public class ScxSocketFrame {

    public long seq_id;
    public byte type;
    public long now;
    public String event_name;
    public String payload;
    public long ack_id;
    public boolean need_ack;
    public boolean need_response;

    public static ScxSocketFrame fromJson(String json) {
        try {
            return JSON_MAPPER.readValue(json, ScxSocketFrame.class);
        } catch (JsonProcessingException e) {
            throw new ScxWrappedRuntimeException(e);
        }
    }

    public String toJson() {
        try {
            return JSON_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ScxWrappedRuntimeException(e);
        }
    }

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
