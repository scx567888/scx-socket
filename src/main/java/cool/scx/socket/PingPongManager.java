package cool.scx.socket;

import io.netty.util.Timeout;
import io.vertx.core.http.WebSocketBase;

import static cool.scx.socket.FrameCreator.PING_FRAME;
import static cool.scx.socket.FrameCreator.PONG_FRAME;
import static cool.scx.socket.ScxSocketFrameType.PING;
import static cool.scx.socket.ScxSocketFrameType.PONG;
import static cool.scx.socket.ScxSocketHelper.setTimeout;
import static java.lang.System.Logger.Level.DEBUG;

public abstract class PingPongManager extends ScxSocket {

    private Timeout ping;
    private Timeout pingTimeout;

    public PingPongManager(ScxSocketOptions options, String clientID) {
        super(options, clientID);
    }

    private void startPingTimeout() {
        cancelPingTimeout();
        this.pingTimeout = setTimeout(this::doPingTimeout, options.getPingTimeout() + options.getPingInterval());
    }

    private void cancelPingTimeout() {
        if (this.pingTimeout != null) {
            this.pingTimeout.cancel();
            this.pingTimeout = null;
        }
    }

    protected void startPing() {
        cancelPing();
        this.ping = setTimeout(() -> {
            sendPing();
            startPing();
        }, options.getPingInterval());
    }

    private void cancelPing() {
        if (this.ping != null) {
            this.ping.cancel();
            this.ping = null;
        }
    }

    @Override
    protected void doSocketFrame(ScxSocketFrame socketFrame) {
        //只要收到任何消息就重置 心跳 
        startPing();
        startPingTimeout();
        switch (socketFrame.type) {
            case PING -> doPing(socketFrame);
            case PONG -> doPong(socketFrame);
            default -> super.doSocketFrame(socketFrame);
        }
    }

    @Override
    void start(WebSocketBase webSocket) {
        super.start(webSocket);
        //启动心跳
        this.startPing();
        //心跳超时
        this.startPingTimeout();
    }

    @Override
    public void close() {
        super.close();
        //取消心跳
        this.cancelPing();
        //取消心跳超时
        this.cancelPingTimeout();
    }

    private void sendPing() {
        var sendPingFuture = this.webSocket.writeTextMessage(PING_FRAME.toJson());

        sendPingFuture.onSuccess(v -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "发送 ping 成功 : {0}", PONG_FRAME.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "发送 ping 失败: {0}", PONG_FRAME.toJson(), c);
            }

        });
    }

    private void sendPong() {
        var sendPongFuture = this.webSocket.writeTextMessage(PONG_FRAME.toJson());

        sendPongFuture.onSuccess(v -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "发送 pong 成功 : {0}", PONG_FRAME.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "发送 pong 失败: {0}", PONG_FRAME.toJson(), c);
            }

        });


    }

    private void doPing(ScxSocketFrame socketFrame) {
        sendPong();

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "收到 ping : {0}", socketFrame.toJson());
        }
    }

    private void doPong(ScxSocketFrame socketFrame) {

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "收到 pong : {0}", socketFrame.toJson());
        }

    }

    protected abstract void doPingTimeout();

}
