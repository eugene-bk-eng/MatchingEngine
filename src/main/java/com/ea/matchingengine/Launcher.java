package com.ea.matchingengine;

import com.ea.matchingengine.config.ConfigReader;
import com.ea.matchingengine.engine.MatchingEngine;
import com.ea.matchingengine.util.UtilReflection;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/

/**
 * Generic application launcher
 */
public class Launcher {

    private final Logger logger = LogManager.getLogger(LoggerNames.getAppLoggerName());

    public Launcher(String args[]) throws Exception {

        logger.info("APP STARTED");
        printProcessInfo();

        // read command line argument
        StartupOptions.ConfigurationProps cfg = (new StartupOptions()).parse(args);
        logger.info("INPUT CONFIG: " + cfg);

        // get configuration
        Configuration configuration = (new ConfigReader()).readConfig(cfg.getConfigFile());
        ConfigReader.print(configuration, logger);

        // launch
        MatchingEngine matchingEngine = UtilReflection.loadInstance(MatchingEngine.class, configuration.getString("program"), Configuration.class, configuration);
        matchingEngine.startMatching();
    }

    public void printProcessInfo() {
        logger.info(String.format("PID: %s", ManagementFactory.getRuntimeMXBean().getPid()));
        logger.info(String.format("TIME ZONE: %s", TimeZone.getDefault().getDisplayName()));
        logger.info(String.format("JVM Name: %s", ManagementFactory.getRuntimeMXBean().getName()));
        logger.info(String.format("JVM Spec: %s", ManagementFactory.getRuntimeMXBean().getSpecName()));
        logger.info(String.format("JVM VERSION: %s", ManagementFactory.getRuntimeMXBean().getSpecVersion()));
        logger.info(String.format("MANAGEMENT SPEC VERSION: %s", ManagementFactory.getRuntimeMXBean().getManagementSpecVersion()));
        logger.info(String.format("VENDOR: %s", ManagementFactory.getRuntimeMXBean().getSpecVendor()));
        logger.info(String.format("START TIME: %s", Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
    }

    public static void main(String[] args) {
        try {
            new Launcher(args);
        } catch (Exception e) {
            // can't use logger here
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
