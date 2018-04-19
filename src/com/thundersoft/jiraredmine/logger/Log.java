package com.thundersoft.jiraredmine.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

    private final static Logger sLogger = LoggerFactory.getLogger(Log.class);

    private Log() {
        
    }

    public static void debug(Class clazz, String msg) {
        sLogger.debug(clazz.getSimpleName() + " : " + msg);
    }

    public static void debug(Class clazz, String msg, Throwable t) {
        sLogger.debug(clazz.getSimpleName() + " : " + msg, t);
    }

    public static void info(Class clazz, String msg) {
        sLogger.info(clazz.getSimpleName() + " : " + msg);
    }

    public static void info(Class clazz, String msg, Throwable t) {
        sLogger.info(clazz.getSimpleName() + " : " + msg, t);
    }
 
    public static void error(Class clazz, String msg) {
        sLogger.error(clazz.getSimpleName() + " : " + msg);
    }

    public static void error(Class clazz, String msg, Throwable t) {
        sLogger.error(clazz.getSimpleName() + " : " + msg, t);
    }
}
