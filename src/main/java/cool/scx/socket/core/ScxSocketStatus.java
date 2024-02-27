package cool.scx.socket.core;

import cool.scx.socket.checker.DuplicateFrameChecker;
import cool.scx.socket.frame.FrameCreator;
import cool.scx.socket.request.RequestManager;
import cool.scx.socket.sender.FrameSender;

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
