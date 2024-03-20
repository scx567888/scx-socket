package cool.scx.socket;


import io.netty.util.Timeout;
import io.vertx.core.http.WebSocketBase;

import static cool.scx.socket.Helper.setTimeout;
import static cool.scx.socket.ScxSocketFrame.Type.PING;
import static cool.scx.socket.ScxSocketFrame.Type.PONG;
import static java.lang.System.Logger.Level.DEBUG;

abstract class PingPongManager extends EasyUseSocket {

    private final PingPongOptions pingPongOptions;
    private Timeout ping;
    private Timeout pingTimeout;

    public PingPongManager(WebSocketBase webSocket, String clientID, PingPongOptions options, ScxSocketStatus status) {
        super(webSocket, clientID, options, status);
        this.pingPongOptions = options;
    }

    public PingPongManager(WebSocketBase webSocket, String clientID, PingPongOptions options) {
        super(webSocket, clientID, options);
        this.pingPongOptions = options;
    }

    private void startPingTimeout() {
        cancelPingTimeout();
        this.pingTimeout = setTimeout(this::doPingTimeout, pingPongOptions.getPingTimeout() + pingPongOptions.getPingInterval());
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
        }, pingPongOptions.getPingInterval());
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
        var sendPingFuture = this.webSocket.writeTextMessage(pingFrame.toJson());

        sendPingFuture.onSuccess(v -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PING 成功 : {1}", clientID, pingFrame.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PING 失败: {1}", clientID, pingFrame.toJson(), c);
            }

        });
    }

    private void sendPong() {
        var pongFrame = status.frameCreator.createPongFrame();
        var sendPongFuture = this.webSocket.writeTextMessage(pongFrame.toJson());

        sendPongFuture.onSuccess(v -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PONG 成功 : {1}", clientID, pongFrame.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PONG 失败 : {1}", clientID, pongFrame.toJson(), c);
            }

        });


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
