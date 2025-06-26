package cool.scx.socket.test;

import cool.scx.logging.ScxLogging;

public class InitLogger {

    static {
        ScxLogging.rootConfig().setLevel(System.Logger.Level.ERROR);
    }

}
