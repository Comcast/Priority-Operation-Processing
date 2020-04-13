package com.comcast.pop.endpoint.api.auth.agenda;

import com.comcast.pop.endpoint.api.agenda.RerunAgendaParameter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class RerunAgendaParameterTest
{
    @Test
    public void testResetAllWithParameter()
    {
        final String VALUE = "theValue";
        Map<RerunAgendaParameter, String> parameterMap =
            RerunAgendaParameter.getParametersMap(Collections.singletonList(RerunAgendaParameter.RESET_ALL.getParameterNameWithValue(VALUE)));
        Assert.assertTrue(parameterMap.containsKey(RerunAgendaParameter.RESET_ALL));
        Assert.assertEquals(parameterMap.get(RerunAgendaParameter.RESET_ALL), VALUE);
    }

    @Test
    public void testInvalidEntries()
    {
        Map<RerunAgendaParameter, String> parameterMap =
            RerunAgendaParameter.getParametersMap(Arrays.asList(
                null,
                "",
                "unknown"
            ));
        Assert.assertTrue(parameterMap.isEmpty());
    }
}
