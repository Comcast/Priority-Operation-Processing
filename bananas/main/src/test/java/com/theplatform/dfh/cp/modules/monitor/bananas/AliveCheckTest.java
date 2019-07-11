package com.theplatform.dfh.cp.modules.monitor.bananas;

import com.theplatform.dfh.cp.modules.monitor.PropertyLoader;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
import com.theplatform.dfh.cp.modules.monitor.bananas.config.BananasConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.bananas.config.BananasPropertiesFactory;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Properties;

public class AliveCheckTest implements AliveCheck
{
    private Boolean[] isAliveState = {  true, false, false, false, true };
    private boolean keepChecking = true;
    private int stateIndex = 0;

    @Override
    public boolean isAlive()
    {
        if(stateIndex == isAliveState.length - 1)
        {
            Assert.assertFalse(true, "exiting... ");
        }
        return isAliveState[stateIndex ++];
    }
    @Test(enabled = false)
    public void testAlive()
    {
        Properties serviceProperties = PropertyLoader.loadResource("../../../../../../service.properties");
        ConfigurationProperties configurationProperties = BananasPropertiesFactory.from(serviceProperties);
        BananasAliveCheckListener bananasAliveCheck = new BananasAliveCheckListener(configurationProperties);
        AliveCheckPoller poller = new AliveCheckPoller(configurationProperties, this, Arrays.asList(bananasAliveCheck));
        poller.start();

        while(keepChecking)
        {
            //keep going
        }
    }
}
