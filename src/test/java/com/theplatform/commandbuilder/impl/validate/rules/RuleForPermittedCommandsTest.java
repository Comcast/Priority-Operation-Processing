package com.theplatform.commandbuilder.impl.validate.rules;

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleForPermittedCommandsTest
{
    @Test
    public void testNothingPermitted()
    {
        RuleForPermittedCommands ruleForPermittedCommands = new RuleForPermittedCommands();

        assertThat(ruleForPermittedCommands.test("rm -rf *")).isFalse();
    }

    @Test
    public void testNullOrEmpty()
    {
        RuleForPermittedCommands ruleForPermittedCommands = new RuleForPermittedCommands();

        assertThat(ruleForPermittedCommands.test(null)).isFalse();
        assertThat(ruleForPermittedCommands.test(" " )).isFalse();
    }

    @Test
    public void testAddForbiddenCommands()
    {
        RuleForPermittedCommands ruleForPermittedCommands = new RuleForPermittedCommands();
        ruleForPermittedCommands.addPermittedCommands(Arrays.asList("ls ", "rm "));

        assertThat(ruleForPermittedCommands.test("rm -rf *")).isTrue();
    }
}