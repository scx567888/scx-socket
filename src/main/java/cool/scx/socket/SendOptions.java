package cool.scx.socket;


/**
 * SendOptions
 *
 * @author scx567888
 * @version 0.0.1
 */
public class SendOptions {

    private boolean needAck;
    private int maxResendTimes;
    private long maxResendDelayed;
    private boolean giveUpIfReachMaxResendTimes;

    public SendOptions() {
        this.needAck = true; // 默认需要 回执
        this.maxResendTimes = 3; // 默认最大发送次数 3 次
        this.maxResendDelayed = 1000 * 10; // 默认最大重发延时 10 秒
        this.giveUpIfReachMaxResendTimes = true; // 默认抛弃 达到最大重发次数的消息
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
