package com.comcast.pop.endpoint.api.auth.agenda;

import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaParameter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class ReigniteAgendaParameterTest
{
    @Test
    public void testResetAllWithParameter()
    {
        final String VALUE = "theValue";
        Map<ReigniteAgendaParameter, String> parameterMap =
            ReigniteAgendaParameter.getParametersMap(Collections.singletonList(ReigniteAgendaParameter.RESET_ALL.getParameterNameWithValue(VALUE)));
        Assert.assertTrue(parameterMap.containsKey(ReigniteAgendaParameter.RESET_ALL));
        Assert.assertEquals(parameterMap.get(ReigniteAgendaParameter.RESET_ALL), VALUE);
    }

    @Test
    public void testInvalidEntries()
    {
        Map<ReigniteAgendaParameter, String> parameterMap =
            ReigniteAgendaParameter.getParametersMap(Arrays.asList(
                null,
                "",
                "unknown"
            ));
        Assert.assertTrue(parameterMap.isEmpty());
    }
}
