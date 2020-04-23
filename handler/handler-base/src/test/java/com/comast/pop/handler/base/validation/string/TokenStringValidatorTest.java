package com.comast.pop.handler.base.validation.string;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class TokenStringValidatorTest
{
    private TokenStringValidator validator;

    @DataProvider
    public Object[][] tokensProvider()
    {
        return new Object[][]
            {
                { null, "", true},
                { null, "aaa", true},
                { Arrays.asList(), "", true},
                { Arrays.asList(), "aaa", true},
                { Arrays.asList("b"), "aaa", false},
                { Arrays.asList("b", "a"), "aaba", true},
                { Arrays.asList("b", "aaa"), "aaab", true},
                { Arrays.asList("-agendaId", "agendaId"), "-agendaId", true},
                // test duplicate tokens (we don't care if that happens)
                { Arrays.asList("-agendaId", "-agendaId"), "-agendaId", true},
            };
    }

    @Test(dataProvider = "tokensProvider")
    public void testValidateString(List<String> tokens, String input, boolean expectedPass)
    {
        setupTokens(tokens);
        try
        {
            validator.validate(input);
            Assert.assertTrue(expectedPass);
        }
        catch(StringValidationException e)
        {
            Assert.assertFalse(expectedPass);
        }

    }

    protected void setupTokens(List<String> tokens)
    {
        validator = new TokenStringValidator(tokens);
    }
}
