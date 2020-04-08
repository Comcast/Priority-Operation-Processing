package com.comcast.pop.commandbuilder.impl.validate.rules;

import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleNoForbiddenCharactersTest {
    @Test
    public void testNothingForbidden()
    {
        RuleNoForbiddenCharacters ruleNoForbiddenCharacters = new RuleNoForbiddenCharacters();

        assertThat(ruleNoForbiddenCharacters.test("abcdefg")).isTrue();
    }

    @Test
    public void testNullAndEmptyNotForbidden()
    {
        RuleNoForbiddenCharacters ruleNoForbiddenCharacters = new RuleNoForbiddenCharacters();

        assertThat(ruleNoForbiddenCharacters.test(null)).isTrue();
        assertThat(ruleNoForbiddenCharacters.test(" ")).isTrue();
    }

    @Test
    public void testAddForbiddenCharacters()
    {
        RuleNoForbiddenCharacters ruleNoForbiddenCharacters = new RuleNoForbiddenCharacters();
        List<Character> characters = new LinkedList<>();
        for(char character : "abyz".toCharArray())
        {
            characters.add(character);
        }
        ruleNoForbiddenCharacters.addForbiddenCharacters(characters);

        assertThat(ruleNoForbiddenCharacters.test("abcdefg")).isFalse();
    }
}