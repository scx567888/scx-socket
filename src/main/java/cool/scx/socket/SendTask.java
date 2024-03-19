package cool.scx.socket;

import cool.scx.common.util.SingleListenerFuture;
import io.netty.util.Timeout;

import java.lang.System.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import static cool.scx.socket.Helper.getDelayed;
import static cool.scx.socket.Helper.setTimeout;
import static java.lang.Math.max;
import static java.lang.System.Logger.Level.DEBUG;

final class SendTask {

    private static final Logger logger = System.getLogger(SendTask.class.getName());

    private final ScxSocketFrame socketFrame;
    private final SendOptions options;
    private final AtomicInteger sendTimes;
    private final FrameSender sender;
    private Timeout resendTask;
    private SingleListenerFuture<Void> sendFuture;

    public SendTask(ScxSocketFrame socketFrame, SendOptions options, FrameSender sender) {
        this.socketFrame = socketFrame;
        this.options = options;
        this.sendTimes = new AtomicInteger(0);
        this.sender = sender;
    }

    public synchronized void start(ScxSocket scxSocket) {
        //当前 websocket 不可用
        if (scxSocket.isClosed()) {
            return;
        }
        //当前已经存在一个 发送中(并未完成发送) 的任务
        if (this.sendFuture != null && !this.sendFuture.isComplete()) {
            return;
        }
        //超过最大发送次数
        if (this.sendTimes.get() > options.getMaxResendTimes()) {
            if (options.getGiveUpIfReachMaxResendTimes()) {
                this.clear();
            }
            return;
        }
        //根据不同序列化配置发送不同消息
        this.sendFuture = new SingleListenerFuture<>(scxSocket.webSocket.writeTextMessage(this.socketFrame.toJson()));

        this.sendFuture.onSuccess(webSocket -> {
            var currentSendTime = sendTimes.getAndIncrement();
            //当需要 ack 时 创建 重复发送 延时
            if (options.getNeedAck()) {
                //计算重新发送延时
                var resendDelayed = max(getDelayed(currentSendTime), options.getMaxResendDelayed());
                this.resendTask = setTimeout(() -> start(scxSocket), resendDelayed);
            } else {
                this.clear();
            }

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送成功 : {1}", scxSocket.clientID(), this.socketFrame.toJson());
            }

        }).onFailure((v) -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送失败 : {1}", scxSocket.clientID(), this.socketFrame.toJson(), v);
            }

        });

    }

    /**
     * 取消重发任务
     */
    public synchronized void cancelResend() {
        this.removeConnectFuture();
        if (this.resendTask != null) {
            this.resendTask.cancel();
            this.resendTask = null;
        }
    }

    /**
     * 从任务列表中移除此任务
     */
    public void clear() {
        cancelResend();
        this.sender.sendTaskMap.remove(socketFrame.seq_id);
    }

    public ScxSocketFrame socketFrame() {
        return this.socketFrame;
    }

    private synchronized void removeConnectFuture() {
        if (this.sendFuture != null) {
            this.sendFuture.onSuccess(null).onFailure(null);
            this.sendFuture = null;
        }
    }

}
