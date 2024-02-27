package cool.scx.socket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class ScxSocketStatus {

    final FrameCreator frameCreator;
    final ConcurrentMap<Long, SendTask> sendTaskMap;
    final DuplicateFrameChecker duplicateFrameChecker;
    final ConcurrentMap<Long, Consumer<String>> responseCallbackMap;

    public ScxSocketStatus(long seqIDClearTimeout) {
        this.frameCreator = new FrameCreator();
        this.sendTaskMap = new ConcurrentHashMap<>();
        this.duplicateFrameChecker = new DuplicateFrameChecker(seqIDClearTimeout);
        this.responseCallbackMap = new ConcurrentHashMap<>();
    }

}
