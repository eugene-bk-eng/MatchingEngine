package com.ea.matchingengine;

import com.ea.matchingengine.config.ConfigReader;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/
public class TestLauncher {

    @Before
    public void setup() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLauncherConfig1() {
        StartupOptions startupOptions=new StartupOptions();
        Assertions.assertThrows(java.lang.NullPointerException.class, () -> {
            StartupOptions.ConfigurationProps cfg = startupOptions.parse(null);
        });
    }

    @Test
    public void testLauncherConfig2() throws ParseException, IOException {
        String args[]=new String[]{"bla"};
        StartupOptions.ConfigurationProps cfg = (new StartupOptions()).parse(args);
        //
        Assert.assertTrue(cfg!=null);
        Assert.assertEquals(null, cfg.getConfigFile() );
    }

    @Test
    public void testLauncherConfig3() throws ParseException, IOException {
        String args[]=new String[]{"-c","myconfig.properties"};
        StartupOptions.ConfigurationProps cfg = (new StartupOptions()).parse(args);
        //
        Assert.assertTrue(cfg!=null);
        Assert.assertEquals("myconfig.properties", cfg.getConfigFile() );
    }

    @Test
    public void testLauncherConfig4() throws ParseException, IOException {
        String args[]=new String[]{"--config","myconfig.properties"};
        StartupOptions.ConfigurationProps cfg = (new StartupOptions()).parse(args);
        //
        Assert.assertTrue(cfg!=null);
        Assert.assertEquals("myconfig.properties", cfg.getConfigFile() );
    }

    @Test
    public void testLauncherConfig5() throws ParseException, IOException, ConfigurationException {
        String args[]=new String[]{"--config","C:\\projects\\java\\personal\\MatchingEngine\\src\\test\\java\\com\\ea\\matchingengine\\config\\dev\\config.properties"};
        StartupOptions.ConfigurationProps cfg = (new StartupOptions()).parse(args);
        //
        Configuration configuration =(new ConfigReader()).readConfig(cfg.getConfigFile());
        Assert.assertEquals("bla", configuration.getString("param1"));
        Assert.assertEquals(5, configuration.getInt("param2"));
        Assert.assertEquals("com.ea.matchingengine.engine.MatchingEngineImpl", configuration.getString("program"));
    }

    @Test
    public void testLauncherConfig6() throws Exception {
        //LogCaptor logCaptorMatchingEngineImpl = LogCaptor.forClass(MatchingEngineImpl.class);
        String args[]=new String[]{"--config","C:\\projects\\java\\personal\\MatchingEngine\\src\\test\\java\\com\\ea\\matchingengine\\config\\dev\\config.properties"};
        Launcher launcher=new Launcher(args);

        //
//        Iterator<String> t=logCaptorMatchingEngineImpl.getInfoLogs().iterator();
//        Assert.assertEquals(3, logCaptorMatchingEngineImpl.getInfoLogs().size());
//        Assert.assertTrue(t.next().endsWith("startMatching"));
//        Assert.assertTrue(t.next().endsWith("initDispatch"));
//        Assert.assertTrue(t.next().endsWith("startDispatch"));
    }
}
