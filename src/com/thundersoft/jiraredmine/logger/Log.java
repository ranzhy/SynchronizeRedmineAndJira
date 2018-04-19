package com.thundersoft.jiraredmine.logger;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

    static {
        String configLocation = "log4j.properties";
        File configFile = new File(configLocation);
        if (!configFile.exists()) {
            System.err.println("log4j2 config file:" + configFile.getAbsolutePath() + " not exist");
            System.exit(0);
        }
        String file = "file://" + configFile.getAbsolutePath();
        System.out.println("log4j2 config file: " + file);

        try {
            URL url = new URL(file);
            System.setProperty("log4j.configuration", url.toString());
        } catch (Exception e) {
            System.err.println("log4j2 initialize error:" + e.getLocalizedMessage());
            System.exit(0);
        }
    }

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
