package com.ea.matchingengine.testutils;

import com.ea.matchingengine.LoggerNames;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is designed to install new appender and new logger which writes
 * into memory. You can then assert the logs as you like.
 * <p>
 * TODO: fix the format and create get info, debug, warning and error line methods.
 */

public class TestLogger {

    private final static String APP_LOGGER_NAME = "junit.app.log";
    private final static String TRADE_LOGGER_NAME = "junit.trade.log";
    private final static Map<String, StringWriter> map = new ConcurrentHashMap();

    public static void createCustomLoggers() {

        LoggerNames.setAppLogName(APP_LOGGER_NAME);
        LoggerNames.setTradeLogName(TRADE_LOGGER_NAME);

        createAppLogger(APP_LOGGER_NAME);
        createAppLogger(TRADE_LOGGER_NAME);
    }

    public static void destroyCustomLoggers() {
        for (String loggerName : map.keySet()) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            config.removeLogger(loggerName);
            ctx.updateLoggers();
        }
        map.clear();
    }

    public static StringWriter getAppLog() {
        return map.get(APP_LOGGER_NAME);
    }

    public static String[] getAppLogLines() {
        String content = map.get(APP_LOGGER_NAME).getBuffer().toString();
        String lines[] = content.split("\\r?\\n");
        return lines;
    }

    private static void createAppLogger(String loggerName) {

        Preconditions.checkArgument(map.containsKey(loggerName) == false);

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %level [%t] [%c] [%M] [%l] - %msg%n").build();

        PatternLayout console_layout = PatternLayout.newBuilder()
                .withPattern("%d{HH:mm:ss.SSS} [%t] %-5.5p %20.30C{1}:%L - %m%n").build();

        StringWriter stringWriter = new StringWriter();
        map.put(loggerName, stringWriter);
        WriterAppender writerAppender = WriterAppender.newBuilder().setName("writeLogger").setTarget(stringWriter).setLayout(layout).build();
        writerAppender.start();
        config.addAppender(writerAppender);

        ConsoleAppender consoleAppender = ConsoleAppender.newBuilder().setName("console-log").setTarget(ConsoleAppender.Target.SYSTEM_OUT).setLayout(console_layout).build();
        consoleAppender.start();
        config.addAppender(consoleAppender);

        AppenderRef ref = AppenderRef.createAppenderRef("writeLogger", Level.DEBUG, null);
        AppenderRef console = AppenderRef.createAppenderRef("console-log", Level.DEBUG, null);
        AppenderRef[] refs = new AppenderRef[]{ref, console};

        LoggerConfig loggerConfig =
                LoggerConfig.createLogger(false, Level.DEBUG, loggerName, "true", refs, null, config, null);

        loggerConfig.addAppender(writerAppender, Level.DEBUG, null);
        loggerConfig.addAppender(consoleAppender, Level.DEBUG, null);
        config.addLogger(loggerName, loggerConfig);
        ctx.updateLoggers();
    }
}