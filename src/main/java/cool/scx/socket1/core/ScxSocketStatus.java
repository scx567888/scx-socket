package cool.scx.socket1.core;

import cool.scx.socket1.checker.DuplicateFrameChecker;
import cool.scx.socket1.frame.FrameCreator;
import cool.scx.socket1.request.RequestManager;
import cool.scx.socket1.sender.FrameSender;

public class ScxSocketStatus {

    final FrameCreator frameCreator;
    final DuplicateFrameChecker duplicateFrameChecker;
    final FrameSender frameSender;
    final RequestManager requestManager;

    public ScxSocketStatus(ScxSocketOptions options) {
        this.frameCreator = new FrameCreator();
        this.frameSender = new FrameSender();
        this.duplicateFrameChecker = new DuplicateFrameChecker(options.getDuplicateFrameCheckerClearTimeout());
        this.requestManager = new RequestManager();
    }

}
