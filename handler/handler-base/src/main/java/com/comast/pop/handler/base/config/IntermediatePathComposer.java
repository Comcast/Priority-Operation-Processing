package com.comast.pop.handler.base.config;

import com.comcast.pop.api.params.ParamsMap;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.validation.string.TokenStringValidatorFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * Composes the operation specific intermediate path string for both creation/retrieval
 */
public class IntermediatePathComposer
{
    protected static final String INTERMEDIATE_PATH_KEY = "pop.handler.intermediate.path";
    protected static final String INTERMEDIATE_REQUIRED_PATH_TOKENS_KEY = "pop.handler.intermediate.requiredPathTokens";
    protected static final String REQUIRED_PATH_TOKEN_DELIMITER = ",";

    private ReferenceTokenReplacer referenceTokenReplacer;
    private TokenStringValidatorFactory tokenStringValidatorFactory;

    public IntermediatePathComposer()
    {
        tokenStringValidatorFactory = new TokenStringValidatorFactory();
        referenceTokenReplacer = new ReferenceTokenReplacer();
    }

    public IntermediatePathComposer setReferenceTokenReplacer(ReferenceTokenReplacer referenceTokenReplacer)
    {
        this.referenceTokenReplacer = referenceTokenReplacer;
        return this;
    }

    public IntermediatePathComposer setTokenStringValidatorFactory(TokenStringValidatorFactory tokenStringValidatorFactory)
    {
        this.tokenStringValidatorFactory = tokenStringValidatorFactory;
        return this;
    }

    /**
     * Gets the intermediate path based on the provided map and environment
     * @param launchDataWrapper The launch data that may optionally contain the required path tokens
     * @param paramsMap The optional map that may contain an intermediate path
     * @param defaultIntermediatePath The fallback intermediate path if none is found
     * @return The intermediate path
     */
    public String retrieveIntermediatePath(LaunchDataWrapper launchDataWrapper, ParamsMap paramsMap, String defaultIntermediatePath)
    {
        if(paramsMap != null)
        {
            String intermediatePath = paramsMap.getString(INTERMEDIATE_PATH_KEY, null);
            if(StringUtils.isNotBlank(intermediatePath))
            {
                validateIntermediatePath(
                    intermediatePath,
                    launchDataWrapper.getPropertyRetriever().getField(INTERMEDIATE_REQUIRED_PATH_TOKENS_KEY, null),
                    launchDataWrapper);
                return intermediatePath;
            }
        }
        return defaultIntermediatePath;
    }

    /**
     * Configures the intermediate path in the params map
     * @param paramsMap The params map to apply the path to
     * @param intermediatePath The intermediate path to persist
     */
    public void configureIntermediatePath(ParamsMap paramsMap, String intermediatePath)
    {
        if(paramsMap == null || intermediatePath == null)
            throw new RuntimeException("paramsMap and path parameter must be non-null");
        paramsMap.put(INTERMEDIATE_PATH_KEY, intermediatePath);
    }

    private void validateIntermediatePath(String intermediatePath, String validationTokens, LaunchDataWrapper launchDataWrapper)
    {
        if(StringUtils.isBlank(validationTokens)) return;
        validationTokens = referenceTokenReplacer.replaceReferenceTokens(validationTokens, launchDataWrapper);
        tokenStringValidatorFactory.create(validationTokens, REQUIRED_PATH_TOKEN_DELIMITER)
            .validate(intermediatePath);
    }
}
