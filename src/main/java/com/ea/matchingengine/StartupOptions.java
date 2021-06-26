package com.ea.matchingengine;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/
public class StartupOptions {

    private static final Logger logger = LogManager.getLogger(StartupOptions.class);
    private final CommandLineParser parser = new DefaultParser();

    public StartupOptions() {
    }

    private Options configureOptions() {
        // create Options object
        Options options = new Options();
        options.addOption("h", "help", false, "Print help");
        options.addOption("c", "config", true, "Path to configuration file");
        return options;
    }

    public ConfigurationProps parse(String[] args) throws ParseException, IOException {
        Preconditions.checkNotNull(args);
        Options options=configureOptions();
        CommandLine cmd = parser.parse( options, args);
        ConfigurationProps result=new ConfigurationProps();

        if( cmd.hasOption("c") || cmd.hasOption("config")  ) {
            result.setConfigFile( cmd.getOptionValue("c"));
        }
        else{
            printUsage(options);
        }
        return result;
    }

    private void printUsage(Options options) throws IOException {
        logger.info("PRINTING USAGE");
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        PrintWriter out=new PrintWriter(bout);
        formatter.printHelp(out,80,"-","USAGE", options, 5,5, "END" );
        out.flush();
        logger.info( bout.toString() );
    }

    public static class ConfigurationProps {
        private  String configFile;

        public String getConfigFile() {
            return configFile;
        }

        public void setConfigFile(String configFile) {
            this.configFile = configFile;
        }
    }
}
