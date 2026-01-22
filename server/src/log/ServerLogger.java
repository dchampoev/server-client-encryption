package log;

import java.io.IOException;
import java.util.logging.*;

public final class ServerLogger {
    private static final Logger LOGGER = Logger.getLogger("ServerLogger");

    static {
        try{
            FileHandler fh = new FileHandler("server.log",true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private ServerLogger() {}

    public static Logger get(){
        return LOGGER;
    }
}
