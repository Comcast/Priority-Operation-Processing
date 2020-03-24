package com.theplatform.dfh.cp.handler.base.config;

import com.theplatform.dfh.cp.api.tokens.AgendaToken;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ReferenceTokenReplacerTest
{
    private ReferenceTokenReplacer replacer;
    private JsonReferenceReplacer jsonReferenceReplacer;
    private LaunchDataWrapper mockLaunchDataWrapper;
    private EnvironmentFieldRetriever mockEnvironmentFieldRetriever;

    @BeforeMethod
    public void setup()
    {
        replacer = new ReferenceTokenReplacer();
        jsonReferenceReplacer = new JsonReferenceReplacer();
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        mockEnvironmentFieldRetriever = mock(EnvironmentFieldRetriever.class);
        doReturn(mockEnvironmentFieldRetriever).when(mockLaunchDataWrapper).getEnvironmentRetriever();
    }

    @Test
    public void testReplaceReferenceTokens()
    {
        final String AGENDA_ID = UUID.randomUUID().toString();
        String tokens = jsonReferenceReplacer.generateReference(AgendaToken.AGENDA_ID.getToken(), null);
        doReturn(AGENDA_ID).when(mockEnvironmentFieldRetriever).getField(anyString(), anyString());
        // no translation should take place
        Assert.assertEquals(replacer.replaceReferenceTokens(tokens, mockLaunchDataWrapper), AGENDA_ID);
    }

    @Test
    public void testReplaceReferenceTokensNoAgendaId()
    {
        String tokens = jsonReferenceReplacer.getReferenceName(AgendaToken.AGENDA_ID.getToken());
        // no translation should take place
        Assert.assertEquals(replacer.replaceReferenceTokens(tokens, mockLaunchDataWrapper), tokens);
    }
}
