package com.hezaerd.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private static final Logger logger = LogManager.getLogger(ModLib.MOD_ID);
    private static boolean debug = Boolean.getBoolean("debug");

    public static void setDebug(boolean value) {
        debug = value;
    }

    public static void i(String s, Object... params) {
        logger.info(s, params);
    }

    public static void w(String s, Object... params) {
        logger.warn(s, params);
    }

    public static void e(String s, Object... params) {
        logger.error(s, params);
    }

    public static void d(String s, Object... params) {
        if (debug) {
            logger.info("[DEBUG] -- " + s, params);
        } else {
            logger.debug(s, params);
        }
    }

    public static void e(String s, Throwable t) {
        logger.error(s, t);
    }

    public static Logger raw() {
        return logger;
    }
}
