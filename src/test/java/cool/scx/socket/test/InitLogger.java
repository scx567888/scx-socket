package cool.scx.socket.test;

import cool.scx.logging.ScxLoggerFactory;

public class InitLogger {

    static {
        ScxLoggerFactory.rootConfig().setLevel(System.Logger.Level.ALL);
    }

}
