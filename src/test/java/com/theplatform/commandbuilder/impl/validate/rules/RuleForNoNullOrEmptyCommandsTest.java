package com.theplatform.commandbuilder.impl.validate.rules;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class RuleForNoNullOrEmptyCommandsTest {

    @Test
    public void testHappy()
    {
        RuleForNoNullOrEmptyCommands nullEmptyRule = new RuleForNoNullOrEmptyCommands();
        assertThat(nullEmptyRule.test("asdfasl ")).isTrue();
    }

    @Test
    public void testNull()
    {
        RuleForNoNullOrEmptyCommands nullEmptyRule = new RuleForNoNullOrEmptyCommands();
        assertThat(nullEmptyRule.test(null)).isFalse();
    }

    @Test
    public void testEmpty()
    {
        RuleForNoNullOrEmptyCommands nullEmptyRule = new RuleForNoNullOrEmptyCommands();
        assertThat(nullEmptyRule.test("  ")).isFalse();
    }
}