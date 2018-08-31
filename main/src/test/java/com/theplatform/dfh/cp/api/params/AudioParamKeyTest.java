package com.theplatform.dfh.cp.api.params;

import org.testng.annotations.Test;

public class AudioParamKeyTest extends BaseParamKeyTest
{
    @Test
    public void testAudioParamKeyMatchesAudioStreamParams()
    {
        testParamKeysMatchParamsObject(AudioParamKey.values(), new AudioStreamParams());
    }
}
