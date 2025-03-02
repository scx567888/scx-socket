package cool.scx.socket;


/**
 * ScxSocketFrame
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class ScxSocketFrame {

    public long seq_id;
    public byte type;
    public long now;
    public String event_name;
    public String payload;
    public long ack_id;
    public boolean need_ack;
    public boolean need_response;

    public static ScxSocketFrame fromJson(String json) {
        return Helper.fromJson(json, ScxSocketFrame.class);
    }

    public String toJson() {
        return Helper.toJson(this);
    }

    public static class Type {
        public static final byte MESSAGE = 0;
        public static final byte RESPONSE = 1;
        public static final byte ACK = 2;
        public static final byte PING = 3;
        public static final byte PONG = 4;
    }

}
