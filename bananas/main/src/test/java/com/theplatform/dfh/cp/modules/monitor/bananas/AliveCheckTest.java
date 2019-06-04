package com.theplatform.dfh.cp.modules.monitor.bananas;

import com.theplatform.dfh.cp.modules.monitor.PropertyLoader;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfiguration;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
import com.theplatform.dfh.cp.modules.monitor.bananas.BananasAliveCheckListener;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class AliveCheckTest implements AliveCheck
{
    private Random random = new Random();

    @Override
    public boolean isAlive()
    {
        return random.nextBoolean();
    }
    @Test(enabled = false)
    public void testAlive()
    {
        Properties serviceProperties = PropertyLoader.loadResource("../../../../../../service.properties");
         BananasAliveCheckListener bananasAliveCheck = new BananasAliveCheckListener(serviceProperties);
        AliveCheckPoller poller = new AliveCheckPoller(new AliveCheckConfiguration(serviceProperties), this, Arrays.asList(bananasAliveCheck));
        poller.start();
    }
}
