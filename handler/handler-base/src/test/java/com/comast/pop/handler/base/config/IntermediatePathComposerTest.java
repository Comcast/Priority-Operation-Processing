package com.comast.pop.handler.base.config;

import com.comcast.pop.api.params.ParamsMap;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comast.pop.handler.base.validation.string.StringValidationException;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class IntermediatePathComposerTest
{
    private final String TOKEN_ONE = "the";
    private final String TOKEN_TWO = "Intermediate";
    private final String TOKEN_THREE = "Path";
    private final String TOKEN_MISSING = "missing";
    private final String INTERMEDIATE_PATH = TOKEN_ONE + TOKEN_TWO + TOKEN_THREE;
    private final String DEFAULT_INTERMEDIATE_PATH = "theDefaultIntermediatePath";

    private IntermediatePathComposer composer;
    private ReferenceTokenReplacer mockReferenceTokenReplacer;
    private LaunchDataWrapper mockLaunchDataWrapper;
    private PropertyRetriever mockPropertyRetriever;
    private ParamsMap paramsMap;

    @BeforeMethod
    public void setup()
    {
        mockReferenceTokenReplacer = mock(ReferenceTokenReplacer.class);
        composer = new IntermediatePathComposer();
        composer.setReferenceTokenReplacer(mockReferenceTokenReplacer);
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        mockPropertyRetriever = mock(PropertyRetriever.class);
        doReturn(mockPropertyRetriever).when(mockLaunchDataWrapper).getPropertyRetriever();
        paramsMap = new ParamsMap();
    }

    @DataProvider
    public Object[][] getIntermediatePathProvider()
    {
        return new Object[][]
            {
                {null, null, DEFAULT_INTERMEDIATE_PATH, DEFAULT_INTERMEDIATE_PATH},
                {null, INTERMEDIATE_PATH, DEFAULT_INTERMEDIATE_PATH, INTERMEDIATE_PATH},
                {TOKEN_ONE, INTERMEDIATE_PATH, DEFAULT_INTERMEDIATE_PATH, INTERMEDIATE_PATH},
                {
                    String.join(IntermediatePathComposer.REQUIRED_PATH_TOKEN_DELIMITER, TOKEN_ONE, TOKEN_TWO, TOKEN_THREE),
                    INTERMEDIATE_PATH,
                    DEFAULT_INTERMEDIATE_PATH,
                    INTERMEDIATE_PATH
                }
            };
    }

    @Test(dataProvider = "getIntermediatePathProvider")
    public void testGetIntermediatePath(String requiredTokens, String intermediatePath, String defaultIntermediatePath, final String EXPECTED_RESULT)
    {
        doReturn(intermediatePath).when(mockReferenceTokenReplacer).replaceReferenceTokens(anyString(), any());
        doReturn(requiredTokens).when(mockPropertyRetriever).getField(IntermediatePathComposer.INTERMEDIATE_REQUIRED_PATH_TOKENS_KEY, null);
        if(StringUtils.isNotBlank(intermediatePath))
            paramsMap.put(IntermediatePathComposer.INTERMEDIATE_PATH_KEY, intermediatePath);
        Assert.assertEquals(composer.retrieveIntermediatePath(mockLaunchDataWrapper, paramsMap, defaultIntermediatePath), EXPECTED_RESULT);
    }

    @Test(expectedExceptions = StringValidationException.class)
    public void testGetInterMediatePathException()
    {
        doReturn(TOKEN_MISSING).when(mockReferenceTokenReplacer).replaceReferenceTokens(anyString(), any());
        doReturn(TOKEN_MISSING).when(mockPropertyRetriever).getField(IntermediatePathComposer.INTERMEDIATE_REQUIRED_PATH_TOKENS_KEY, null);
        paramsMap.put(IntermediatePathComposer.INTERMEDIATE_PATH_KEY, INTERMEDIATE_PATH);
        composer.retrieveIntermediatePath(mockLaunchDataWrapper, paramsMap, DEFAULT_INTERMEDIATE_PATH);
    }
}
