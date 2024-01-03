package cool.scx.socket;

public final class SendOptions {

    static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();

    private boolean needAck;

    private int maxResendTimes;

    private long maxResendDelayed;

    private boolean giveUpIfReachMaxResendTimes;

    public SendOptions() {
        this.needAck = true;
        this.maxResendTimes = 3;
        this.maxResendDelayed = 1000 * 10;
        this.giveUpIfReachMaxResendTimes = true;
    }

    public boolean getNeedAck() {
        return needAck;
    }

    public SendOptions setNeedAck(boolean needAck) {
        this.needAck = needAck;
        return this;
    }

    public int getMaxResendTimes() {
        return maxResendTimes;
    }

    public SendOptions setMaxResendTimes(int maxResendTimes) {
        this.maxResendTimes = maxResendTimes;
        return this;
    }

    public long getMaxResendDelayed() {
        return maxResendDelayed;
    }

    public SendOptions setMaxResendDelayed(long maxDelayed) {
        this.maxResendDelayed = maxDelayed;
        return this;
    }

    public boolean getGiveUpIfReachMaxResendTimes() {
        return giveUpIfReachMaxResendTimes;
    }

    public SendOptions setGiveUpIfReachMaxResendTimes(boolean giveUpIfReachMaxResendTimes) {
        this.giveUpIfReachMaxResendTimes = giveUpIfReachMaxResendTimes;
        return this;
    }

}
