package cool.scx.socket;

public final class ScxSocketRequest {

    private final ScxSocket scxSocket;
    private final long ack_id;
    private boolean alreadyResponse;

    public ScxSocketRequest(ScxSocket scxSocket, long ack_id) {
        this.scxSocket = scxSocket;
        this.ack_id = ack_id;
        this.alreadyResponse = false;
    }

    public void response(String payload) {
        if (alreadyResponse) {
            throw new UnsupportedOperationException("已经响应过 !!!");
        } else {
            alreadyResponse = true;
            scxSocket.sendResponse(ack_id, payload);
        }
    }

}
