package cool.scx.socket;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 包裹具体事件
 */
final class EventHandler {

    public final byte type;
    private Consumer<String> event0;
    private Function<String, String> event1;
    private BiConsumer<String, ScxSocketRequest> event2;

    public EventHandler(Consumer<String> eventConsumer) {
        this.event0 = eventConsumer;
        this.type = 0;
    }

    public EventHandler(Function<String, String> eventFunction) {
        this.event1 = eventFunction;
        this.type = 1;
    }

    public EventHandler(BiConsumer<String, ScxSocketRequest> eventBiConsumer) {
        this.event2 = eventBiConsumer;
        this.type = 2;
    }

    public Consumer<String> event0() {
        return event0;
    }

    public Function<String, String> event1() {
        return event1;
    }

    public BiConsumer<String, ScxSocketRequest> event2() {
        return event2;
    }

}
