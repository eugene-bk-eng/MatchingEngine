package com.ea.matchingengine.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.net.MalformedURLException;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/
public class ConfigReader {

    public ConfigReader(){
    }

    public Configuration readConfig(String propertyFile) throws ConfigurationException, MalformedURLException {
        Configurations configs = new Configurations();
        Configuration config = configs.properties(new File(propertyFile));
        return config;
    }
}
