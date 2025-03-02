package cool.scx.socket;

import java.util.concurrent.atomic.AtomicLong;

import static cool.scx.socket.ScxSocketFrame.Type.*;


/**
 * FrameCreator
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class FrameCreator {

    private final AtomicLong nowSeqID;

    public FrameCreator() {
        this.nowSeqID = new AtomicLong(0);
    }

    public ScxSocketFrame createPingFrame() {
        var pingFrame = new ScxSocketFrame();
        pingFrame.type = PING;
        return pingFrame;
    }

    public ScxSocketFrame createPongFrame() {
        var pongFrame = new ScxSocketFrame();
        pongFrame.type = PONG;
        return pongFrame;
    }

    public ScxSocketFrame createAckFrame(long ack_id) {
        var ackFrame = new ScxSocketFrame();
        ackFrame.type = ACK;
        ackFrame.ack_id = ack_id;
        return ackFrame;
    }

    public ScxSocketFrame createAckFrame(long ack_id, String payload) {
        var ackFrame = createAckFrame(ack_id);
        ackFrame.payload = payload;
        return ackFrame;
    }

    private ScxSocketFrame createBaseFrame(String content, SendOptions options) {
        var baseFrame = new ScxSocketFrame();
        baseFrame.seq_id = this.nowSeqID.getAndIncrement();
        baseFrame.now = System.currentTimeMillis();
        baseFrame.need_ack = options.getNeedAck();
        baseFrame.payload = content;
        return baseFrame;
    }

    public ScxSocketFrame createMessageFrame(String content, SendOptions options) {
        var messageFrame = createBaseFrame(content, options);
        messageFrame.type = MESSAGE;
        return messageFrame;
    }

    public ScxSocketFrame createEventFrame(String eventName, String payload, SendOptions options) {
        var eventFrame = createMessageFrame(payload, options);
        eventFrame.event_name = eventName;
        return eventFrame;
    }

    public ScxSocketFrame createRequestFrame(String eventName, String payload, SendOptions options) {
        var requestFrame = createEventFrame(eventName, payload, options);
        requestFrame.need_response = true;
        return requestFrame;
    }

    public ScxSocketFrame createResponseFrame(long ack_id, String payload, SendOptions options) {
        var responseFrame = createBaseFrame(payload, options);
        responseFrame.type = RESPONSE;
        responseFrame.ack_id = ack_id;
        return responseFrame;
    }

}
