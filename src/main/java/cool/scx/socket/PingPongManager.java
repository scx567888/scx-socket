package cool.scx.socket;


import cool.scx.http.web_socket.ScxWebSocket;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static cool.scx.socket.ScxSocketFrame.Type.PING;
import static cool.scx.socket.ScxSocketFrame.Type.PONG;
import static java.lang.System.Logger.Level.DEBUG;


/**
 * PingPongManager
 *
 * @author scx567888
 * @version 0.0.1
 */
abstract class PingPongManager extends EasyUseSocket {

    private final PingPongOptions pingPongOptions;
    private ScheduledFuture<?> ping;
    private ScheduledFuture<?> pingTimeout;

    public PingPongManager(ScxWebSocket webSocket, String clientID, PingPongOptions options, ScxSocketStatus status) {
        super(webSocket, clientID, options, status);
        this.pingPongOptions = options;
    }

    public PingPongManager(ScxWebSocket webSocket, String clientID, PingPongOptions options) {
        super(webSocket, clientID, options);
        this.pingPongOptions = options;
    }

    private void startPingTimeout() {
        cancelPingTimeout();
        this.pingTimeout = scheduledExecutor.schedule(this::doPingTimeout, pingPongOptions.getPingTimeout() + pingPongOptions.getPingInterval(), TimeUnit.MILLISECONDS);
    }

    private void cancelPingTimeout() {
        if (this.pingTimeout != null) {
            this.pingTimeout.cancel(false);
            this.pingTimeout = null;
        }
    }

    protected void startPing() {
        cancelPing();
        this.ping = scheduledExecutor.schedule(() -> {
            sendPing();
            startPing();
        }, pingPongOptions.getPingInterval(), TimeUnit.MILLISECONDS);
    }

    private void cancelPing() {
        if (this.ping != null) {
            this.ping.cancel(false);
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
    protected void start() {
        super.start();
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
        var pingFrame = status.frameCreator.createPingFrame();

        try {

            this.webSocket.send(pingFrame.toJson());

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PING 成功 : {1}", clientID, pingFrame.toJson());
            }

        } catch (Exception e) {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PING 失败: {1}", clientID, pingFrame.toJson(), e);
            }

        }
    }

    private void sendPong() {
        var pongFrame = status.frameCreator.createPongFrame();

        try {

            this.webSocket.send(pongFrame.toJson());

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PONG 成功 : {1}", clientID, pongFrame.toJson());
            }

        } catch (Exception e) {


            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PONG 失败 : {1}", clientID, pongFrame.toJson(), e);
            }

        }

    }

    private void doPing(ScxSocketFrame socketFrame) {
        sendPong();

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到 PING : {1}", clientID, socketFrame.toJson());
        }
    }

    private void doPong(ScxSocketFrame socketFrame) {

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到 PONG : {1}", clientID, socketFrame.toJson());
        }

    }

    protected abstract void doPingTimeout();

}
