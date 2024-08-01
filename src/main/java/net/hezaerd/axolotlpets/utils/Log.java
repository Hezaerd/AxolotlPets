package net.hezaerd.axolotlpets.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {
    public static final Logger logger = LoggerFactory.getLogger("AxolotlPets");
    private static boolean debug = "true".equals(System.getProperty("debug"));

    public static void i(String s) {
        logger.info(s);
    }

    public static void w(String s) {
        logger.warn(s);
    }

    public static void e(String s) {
        logger.error(s);
    }

    public static void d(String s) {
        if (debug) {
            i("[DEBUG] -- " + s);
        } else {
            logger.debug(s);
        }
    }

    public static void i(String s, Object... params) {
        logger.info(String.format(s, params));
    }

    public static void w(String s, Object... params) {
        logger.warn(String.format(s, params));
    }

    public static void e(String s, Object... params) {
        logger.error(String.format(s, params));
    }

    public static void d(String s, Object... params) {
        if (debug) {
            i("[DEBUG] -- " + String.format(s, params));
        } else {
            logger.debug(String.format(s, params));
        }
    }

    public static void i(Object s) {
        logger.info(s.toString());
    }

    public static void w(Object s) {
        logger.warn(s.toString());
    }

    public static void e(Object s) {
        logger.error(s.toString());
    }

    public static Logger raw() {
        return logger;
    }
}
