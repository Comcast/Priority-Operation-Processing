package com.comcast.pop.commandbuilder.impl.validate;

import com.comcast.pop.commandbuilder.impl.validate.rules.RuleNoForbiddenCommands;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseCommandValidatorTest
{
    @Test
    public void testValidateWithNoRules()
    {
        BaseCommandValidator validator = new BaseCommandValidator();

        assertThat(validator.validate("rm -rf *")).isTrue();
    }

    @Test
    public void testSetRule()
    {
        BaseCommandValidator validator = new BaseCommandValidator();
        validator.setRule(makeRule());

        assertThat(validator.validate("rm -rf *")).isFalse();
        assertThat(validator.validate("ls -l")).isTrue();
    }

    private Predicate<String> makeRule() {
        RuleNoForbiddenCommands ruleNoForbiddenCommands = new RuleNoForbiddenCommands();
        ruleNoForbiddenCommands.addForbiddenCommands(Arrays.asList("echo ", "rm "));
        return ruleNoForbiddenCommands;
    }
}