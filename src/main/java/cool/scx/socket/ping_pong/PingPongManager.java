package cool.scx.socket.ping_pong;

import cool.scx.socket.core.ScxSocket;
import cool.scx.socket.frame.ScxSocketFrame;
import io.netty.util.Timeout;

import java.lang.System.Logger;

import static cool.scx.socket.frame.FrameCreator.PING_FRAME;
import static cool.scx.socket.frame.FrameCreator.PONG_FRAME;
import static cool.scx.socket.helper.Helper.setTimeout;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

/**
 * 心跳管理器
 */
public final class PingPongManager {

    private static final Logger logger = getLogger(PingPongManager.class.getName());

    private final PingPongOptions pingPongOptions;
    private final ScxSocket scxSocket;
    private Timeout ping;
    private Timeout pingTimeout;
    private final Runnable onPingTimeout;

    public PingPongManager(Runnable onPingTimeout, ScxSocket scxSocket, PingPongOptions options) {
        this.onPingTimeout = onPingTimeout;
        this.scxSocket = scxSocket;
        this.pingPongOptions = options;
    }

    public void startPingTimeout() {
        cancelPingTimeout();
        this.pingTimeout = setTimeout(this::_callOnPingTimeout, pingPongOptions.getPingTimeout() + pingPongOptions.getPingInterval());
    }

    public void cancelPingTimeout() {
        if (this.pingTimeout != null) {
            this.pingTimeout.cancel();
            this.pingTimeout = null;
        }
    }

    public void startPing() {
        cancelPing();
        this.ping = setTimeout(() -> {
            sendPing();
            startPing();
        }, pingPongOptions.getPingInterval());
    }

    public void cancelPing() {
        if (this.ping != null) {
            this.ping.cancel();
            this.ping = null;
        }
    }

    private void sendPing() {
        var sendPingFuture = this.scxSocket.webSocket.writeTextMessage(PING_FRAME.toJson());

        sendPingFuture.onSuccess(v -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PING 成功 : {1}", scxSocket.clientID, PONG_FRAME.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PING 失败: {1}", scxSocket.clientID, PONG_FRAME.toJson(), c);
            }

        });
    }

    private void sendPong() {
        var sendPongFuture = this.scxSocket.webSocket.writeTextMessage(PONG_FRAME.toJson());

        sendPongFuture.onSuccess(v -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PONG 成功 : {1}", scxSocket.clientID, PONG_FRAME.toJson());
            }

        }).onFailure(c -> {

            //LOGGER
            if (logger.isLoggable(DEBUG)) {
                logger.log(DEBUG, "CLIENT_ID : {0}, 发送 PONG 失败 : {1}", scxSocket.clientID, PONG_FRAME.toJson(), c);
            }

        });


    }

    public void doPing(ScxSocketFrame socketFrame) {
        sendPong();

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到 PING : {1}", scxSocket.clientID, socketFrame.toJson());
        }
    }

    public void doPong(ScxSocketFrame socketFrame) {

        //LOGGER
        if (logger.isLoggable(DEBUG)) {
            logger.log(DEBUG, "CLIENT_ID : {0}, 收到 PONG : {1}", scxSocket.clientID, socketFrame.toJson());
        }

    }

    private void _callOnPingTimeout() {
        if (this.onPingTimeout != null) {
            this.onPingTimeout.run();
        }
    }

}
