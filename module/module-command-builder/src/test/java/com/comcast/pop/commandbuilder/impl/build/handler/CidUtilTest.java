package com.comcast.pop.commandbuilder.impl.build.handler;

import com.comcast.pop.commandbuilder.impl.build.handler.utilities.CidUtil;
import org.testng.annotations.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CidUtilTest
{
    @Test
    public void testCidUtilDefaults()
    {
        String cid = CidUtil.getCid();
        assertSoftly(softly ->
        {
            softly.assertThat(cid).contains(CidUtil.DEFAULT_HANDLER);
            softly.assertThat(cid).contains(CidUtil.TEST_CID);
        });
    }

}