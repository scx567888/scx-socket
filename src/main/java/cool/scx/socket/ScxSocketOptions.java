package cool.scx.socket;

public class ScxSocketOptions {

    /**
     * 重复帧检查器清除超时
     */
    private int duplicateFrameCheckerClearTimeout;

    public ScxSocketOptions() {
        this.duplicateFrameCheckerClearTimeout = 1000 * 60 * 10;// 默认 10 分钟
    }

    public int getDuplicateFrameCheckerClearTimeout() {
        return duplicateFrameCheckerClearTimeout;
    }

    public ScxSocketOptions setDuplicateFrameCheckerClearTimeout(int duplicateFrameCheckerClearTimeout) {
        this.duplicateFrameCheckerClearTimeout = duplicateFrameCheckerClearTimeout;
        return this;
    }

}
