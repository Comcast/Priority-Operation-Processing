package com.theplatform.commandbuilder.impl.build.handler;

import com.theplatform.commandbuilder.impl.build.handler.utilities.CidUtil;
import org.testng.annotations.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CidUtilImmutability
{
    @Test
    public void testhandlerImmutability()
    {
        String testHandler = "testHandler";
        String mutateHandlerValue = "something else";
        CidUtil.setHandler(testHandler);
        CidUtil.setHandler(mutateHandlerValue);
        String cid = CidUtil.getCid();
        assertSoftly(softly ->
        {
            softly.assertThat(cid).contains(testHandler);
            softly.assertThat(cid).doesNotContain(mutateHandlerValue);
        });
    }
}
