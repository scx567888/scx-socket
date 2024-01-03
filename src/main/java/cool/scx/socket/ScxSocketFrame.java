package cool.scx.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import cool.scx.util.ScxExceptionHelper.ScxWrappedRuntimeException;

import static cool.scx.socket.ScxSocketHelper.JSON_MAPPER;

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

}
