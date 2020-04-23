package com.comast.pop.handler.base.field.retriever.environment;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EnvironmentFieldRetrieverTest
{
    final String PAYLOAD = "payload";

    final String DEFAULT_VALUE = "foundit!";
    final String FIELD_VALUE = "field value!";

    EnvironmentVariableProvider mockEnvironmentVariableProvider;
    EnvironmentFieldRetriever fieldRetriever;

    @BeforeMethod
    public void setup()
    {
        mockEnvironmentVariableProvider = mock(EnvironmentVariableProvider.class);
        fieldRetriever = new EnvironmentFieldRetriever();
        fieldRetriever.setEnvironmentVariableProvider(mockEnvironmentVariableProvider);
    }

    @Test void testVariableFound()
    {
        doReturn(FIELD_VALUE).when(mockEnvironmentVariableProvider).getVariable(PAYLOAD);
        Assert.assertEquals(fieldRetriever.getField(PAYLOAD), FIELD_VALUE);
    }

    @Test void testVariableFoundWithDefault()
    {
        doReturn(FIELD_VALUE).when(mockEnvironmentVariableProvider).getVariable(PAYLOAD);
        Assert.assertEquals(fieldRetriever.getField(PAYLOAD, DEFAULT_VALUE), FIELD_VALUE);
    }

    @Test
    public void testMissingVariableDefault()
    {

        Assert.assertEquals(fieldRetriever.getField(PAYLOAD, DEFAULT_VALUE), DEFAULT_VALUE);
    }

    @Test
    public void testMissingVariable()
    {
        Assert.assertNull(fieldRetriever.getField(PAYLOAD));
    }

}
