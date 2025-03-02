package cool.scx.socket;


/**
 * ScxSocketStatus
 *
 * @author scx567888
 * @version 0.0.1
 */
final class ScxSocketStatus {

    final FrameCreator frameCreator;
    final DuplicateFrameChecker duplicateFrameChecker;
    final FrameSender frameSender;
    final RequestManager requestManager;

    public ScxSocketStatus(ScxSocketOptions options) {
        this.frameCreator = new FrameCreator();
        this.frameSender = new FrameSender(options);
        this.duplicateFrameChecker = new DuplicateFrameChecker(options);
        this.requestManager = new RequestManager(options);
    }

}
