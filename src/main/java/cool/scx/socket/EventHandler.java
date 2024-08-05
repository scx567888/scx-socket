package cool.scx.socket;

/**
 * 包裹具体事件
 */
public interface EventHandler {

    void handle(ScxSocketRequest socketRequest);

}
