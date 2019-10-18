package com.theplatform.dfh.cp.handler.base.validation.string;

import com.theplatform.dfh.cp.handler.base.config.ReferenceTokenReplacer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Validator to check that all specified tokens are present
 */
public class TokenStringValidator implements StringValidator
{
    private static Logger logger = LoggerFactory.getLogger(TokenStringValidator.class);
    private Collection<String> tokens;

    public TokenStringValidator(Collection<String> tokens)
    {
        this.tokens = tokens;
    }

    @Override
    public void validate(String input)
    {
        if(tokens == null) return;
        logger.info("Verifying input [{}] contains tokens: [{}]", input, String.join(",", tokens));
        if(!tokens.stream().allMatch(token -> StringUtils.containsIgnoreCase(input, token)))
            throw new StringValidationException(String.format("Input [%1$s] is missing a required token.", input));
    }
}
