package cool.scx.socket;

import com.fasterxml.jackson.core.type.TypeReference;

import static cool.scx.socket.Helper.fromJson;


/**
 * ScxSocketResponse
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class ScxSocketResponse {

    private final String payload;
    private final RuntimeException cause;

    public ScxSocketResponse(String payload) {
        this.payload = payload;
        this.cause = null;
    }

    public ScxSocketResponse(RuntimeException cause) {
        this.payload = null;
        this.cause = cause;
    }

    public boolean isSuccess() {
        return cause == null;
    }

    public String payload() {
        return payload;
    }

    public <T> T payload(TypeReference<T> valueTypeRef) {
        return fromJson(payload(), valueTypeRef);
    }

    public RuntimeException cause() {
        return cause;
    }

}
