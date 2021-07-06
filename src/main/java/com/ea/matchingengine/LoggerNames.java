package com.ea.matchingengine;

/**
 * @author : eugene
 * @created : 6/27/2021, Sunday
 **/
public class LoggerNames {

    public static String APP_LOG_NAME = "async.app.log";
    public static String TRADE_LOG_NAME = "async.trade.log";

    public static synchronized String getAppLoggerName() {
        return APP_LOG_NAME;
    }

    public static synchronized String getTradeLoggerName() {
        return TRADE_LOG_NAME;
    }

    public static void setAppLogName(String appLogName) {
        APP_LOG_NAME = appLogName;
    }

    public static void setTradeLogName(String tradeLogName) {
        TRADE_LOG_NAME = tradeLogName;
    }
}
