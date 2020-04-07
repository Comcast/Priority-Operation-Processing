package com.theplatform.dfh.cp.api.params;

import org.testng.annotations.Test;

public class VideoParamKeyTest extends BaseParamKeyTest
{
    @Test
    public void testVideoParamKeyMatchesVideoStreamParams()
    {
        testParamKeysMatchParamsObject(VideoParamKey.values(), new VideoStreamParams());
    }
}