package com.comast.pop.handler.base.validation.string;

import java.util.Arrays;

/**
 * Convenience factory for generating TokenStringValidators
 */
public class TokenStringValidatorFactory
{
    public TokenStringValidator create(String[] tokens)
    {
        return new TokenStringValidator(Arrays.asList(tokens));
    }

    public TokenStringValidator create(String tokens, String delimiter)
    {
        return create(tokens.split(delimiter));
    }
}
