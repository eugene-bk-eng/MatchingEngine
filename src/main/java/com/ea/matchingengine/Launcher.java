package com.ea.matchingengine;

import com.ea.matchingengine.config.ConfigReader;
import com.ea.matchingengine.engine.MatchingEngine;
import com.ea.matchingengine.util.UtilReflection;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/

/**
 * Generic application launcher
 */
public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public Launcher(String args[]) throws Exception {

        // read command line argument
        StartupOptions.ConfigurationProps cfg = (new StartupOptions()).parse(args);
        logger.info("INPUT CONFIG: " + cfg);

        // get configuration
        Configuration configuration =(new ConfigReader()).readConfig(cfg.getConfigFile());
        ConfigReader.print(configuration,logger);

        // launch
        MatchingEngine matchingEngine=UtilReflection.loadInstance(MatchingEngine.class, configuration.getString("program"), Configuration.class, configuration );
        matchingEngine.startMatching();
    }

    public static void main(String[] args) {
        try {
            new Launcher(args);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
    }
}
