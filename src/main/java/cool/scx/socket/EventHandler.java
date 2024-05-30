package cool.scx.socket;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 包裹具体事件
 */
public interface EventHandler {

    void handle(ScxSocketFrame socketFrame, ScxSocketRequest socketRequest);

    record RunnableEventHandler(Runnable event) implements EventHandler {

        @Override
        public void handle(ScxSocketFrame socketFrame, ScxSocketRequest socketRequest) {
            event.run();
            if (socketFrame.need_response) {
                socketRequest.response(null);
            }
        }

    }

    record ConsumerEventHandler(Consumer<String> event) implements EventHandler {

        @Override
        public void handle(ScxSocketFrame socketFrame, ScxSocketRequest socketRequest) {
            event.accept(socketFrame.payload);
            if (socketFrame.need_response) {
                socketRequest.response(null);
            }
        }

    }

    record SupplierEventHandler(Supplier<String> event) implements EventHandler {

        @Override
        public void handle(ScxSocketFrame socketFrame, ScxSocketRequest socketRequest) {
            var responseData = event.get();
            if (socketFrame.need_response) {
                socketRequest.response(responseData);
            }
        }

    }

    record FunctionEventHandler(Function<String, String> event) implements EventHandler {

        @Override
        public void handle(ScxSocketFrame socketFrame, ScxSocketRequest socketRequest) {
            var responseData = event.apply(socketFrame.payload);
            if (socketFrame.need_response) {
                socketRequest.response(responseData);
            }
        }

    }

    record BiConsumerEventHandler(BiConsumer<String, ScxSocketRequest> event) implements EventHandler {

        @Override
        public void handle(ScxSocketFrame socketFrame, ScxSocketRequest socketRequest) {
            if (socketFrame.need_response) {
                event.accept(socketFrame.payload, socketRequest);
            } else {
                event.accept(socketFrame.payload, null);
            }
        }

    }

}
