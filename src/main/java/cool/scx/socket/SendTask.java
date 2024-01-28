package cool.scx.socket;

import cool.scx.util.ScxFuture;
import io.netty.util.Timeout;

import java.lang.System.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import static cool.scx.socket.ScxSocketHelper.getDelayed;
import static cool.scx.socket.ScxSocketHelper.setTimeout;
import static java.lang.Math.max;
import static java.lang.System.Logger.Level.DEBUG;

public final class SendTask {

    private static final Logger logger = System.getLogger(SendTask.class.getName());

    private final ScxSocketFrame socketFrame;
    private final SendOptions options;
    private final ScxSocket scxSocket;
    private final AtomicInteger sendTimes;
    private Timeout resendThread;
    private ScxFuture<Void> sendFuture;

    public SendTask(ScxSocketFrame socketFrame, SendOptions options, ScxSocket scxSocket) {
        this.socketFrame = socketFrame;
        this.options = options;
        this.scxSocket = scxSocket;
        this.sendTimes = new AtomicInteger(0);
    }

    public void start() {
        //当前 websocket 不可用
        if (this.scxSocket.isClosed()) {
            return;
        }
        //当前已经存在一个 发送中(并未完成发送) 的任务
        if (this.sendFuture != null && !this.sendFuture.isComplete()) {
            return;
        }
        //超过最大发送次数
        if (this.sendTimes.get() > options.getMaxResendTimes()) {
            if (options.getGiveUpIfReachMaxResendTimes()) {
                clear();
            }
            return;
        }
        //根据不同序列化配置发送不同消息
        this.sendFuture = new ScxFuture<>(scxSocket.webSocket.writeTextMessage(this.socketFrame.toJson()));

        this.sendFuture.onSuccess(webSocket -> {
            var currentSendTime = sendTimes.getAndIncrement();
            //当需要 ack 时 创建 重复发送 延时
            if (options.getNeedAck()) {
                this.resendThread = setTimeout(this::start, max(getDelayed(currentSendTime), options.getMaxResendDelayed()));
            } else {
                clear();
            }

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "clientID : {0}, 发送成功 : {1}",this.scxSocket.clientID, this.socketFrame.toJson());
            }

        }).onFailure((v) -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "clientID : {0}, 发送失败 : {1}",this.scxSocket.clientID, this.socketFrame.toJson(), v);
            }

        });

    }

    /**
     * 取消重发任务
     */
    public void cancelResend() {
        removeConnectFuture();
        if (this.resendThread != null) {
            this.resendThread.cancel();
            this.resendThread = null;
        }
    }

    /**
     * 从任务列表中移除此任务
     */
    public void clear() {
        cancelResend();
        this.scxSocket.sendTaskMap.remove(socketFrame.seq_id);
    }

    public ScxSocketFrame socketFrame() {
        return socketFrame;
    }

    private void removeConnectFuture() {
        if (this.sendFuture != null) {
            this.sendFuture.onSuccess(null).onFailure(null);
            this.sendFuture = null;
        }
    }

}
