package cool.scx.socket;

import cool.scx.reflect.TypeReference;

import static cool.scx.socket.Helper.fromJson;
import static cool.scx.socket.Helper.toJson;


/**
 * ScxSocketRequest
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class ScxSocketRequest {

    private final ScxSocket scxSocket;
    private final ScxSocketFrame socketFrame;
    private boolean alreadyResponse;

    public ScxSocketRequest(ScxSocket scxSocket, ScxSocketFrame socketFrame) {
        this.scxSocket = scxSocket;
        this.socketFrame = socketFrame;
        this.alreadyResponse = false;
    }

    ScxSocketFrame socketFrame() {
        return socketFrame;
    }

    public void response(String payload) {
        if (alreadyResponse) {
            throw new UnsupportedOperationException("已经响应过 !!!");
        } else {
            alreadyResponse = true;
            scxSocket.sendResponse(socketFrame.seq_id, payload);
        }
    }

    public void response(Object payload) {
        this.response(toJson(payload));
    }

    public String payload() {
        return socketFrame.payload;
    }

    public <T> T payload(TypeReference<T> valueTypeRef) {
        return fromJson(payload(), valueTypeRef);
    }

}
