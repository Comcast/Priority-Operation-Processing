package com.theplatform.dfh.endpoint.api.agenda.service;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class RetryAgendaParameterTest
{
    @Test
    public void testResetAllWithParameter()
    {
        final String VALUE = "theValue";
        Map<RetryAgendaParameter, String> parameterMap =
            RetryAgendaParameter.getParametersMap(Collections.singletonList(RetryAgendaParameter.RESET_ALL.getParameterNameWithValue(VALUE)));
        Assert.assertTrue(parameterMap.containsKey(RetryAgendaParameter.RESET_ALL));
        Assert.assertEquals(parameterMap.get(RetryAgendaParameter.RESET_ALL), VALUE);
    }

    @Test
    public void testInvalidEntries()
    {
        Map<RetryAgendaParameter, String> parameterMap =
            RetryAgendaParameter.getParametersMap(Arrays.asList(
                null,
                "",
                "unknown"
            ));
        Assert.assertTrue(parameterMap.isEmpty());
    }
}
