package cool.scx.socket;

public class ScxSocketRequest {

    private final ScxSocketBase scxSocketBase;

    private final long ack_id;

    private boolean alreadyResponse;

    public ScxSocketRequest(ScxSocketBase scxSocketBase, long ack_id) {
        this.scxSocketBase = scxSocketBase;
        this.ack_id = ack_id;
        this.alreadyResponse = false;
    }

    public void response(String payload) {
        if (alreadyResponse) {
            throw new UnsupportedOperationException("已经响应过 !!!");
        } else {
            alreadyResponse = true;
            scxSocketBase.sendResponse(ack_id, payload);
        }
    }

}
