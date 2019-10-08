package com.theplatform.commandbuilder.impl.validate.rules;

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleNoForbiddenCommandsTest
{
    @Test
    public void testNothingForbidden()
    {
        RuleNoForbiddenCommands ruleNoForbiddenCommands = new RuleNoForbiddenCommands();

        assertThat(ruleNoForbiddenCommands.test("rm -rf *")).isTrue();
    }

    @Test
    public void testNullAndEmpty()
    {
        RuleNoForbiddenCommands ruleNoForbiddenCommands = new RuleNoForbiddenCommands();

        assertThat(ruleNoForbiddenCommands.test(null)).isTrue();
        assertThat(ruleNoForbiddenCommands.test(" " )).isTrue();
    }


    @Test
    public void testAddForbiddenCommands()
    {
        RuleNoForbiddenCommands ruleNoForbiddenCommands = new RuleNoForbiddenCommands();
        ruleNoForbiddenCommands.addForbiddenCommands(Arrays.asList("ls ", "rm "));

        assertThat(ruleNoForbiddenCommands.test("rm -rf *")).isFalse();
    }

    @Test
    public void testCaptializationInvariant()
    {
        RuleNoForbiddenCommands ruleNoForbiddenCommands = new RuleNoForbiddenCommands();
        ruleNoForbiddenCommands.addForbiddenCommands(Arrays.asList("ls ", "Rm "));

        assertThat(ruleNoForbiddenCommands.test("rm -rf *")).isFalse();
    }
}