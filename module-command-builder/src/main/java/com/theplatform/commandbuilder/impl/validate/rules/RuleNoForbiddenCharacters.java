package com.theplatform.commandbuilder.impl.validate.rules;

import java.util.*;
import java.util.function.Predicate;

/**
 * Rule is not testing for nulls.  If desired, null check should be its own rule.
 */
public class RuleNoForbiddenCharacters implements Predicate<String[]> {

    private Set<Character> forbiddenCharacters = new HashSet<>();

    @Override
    public boolean test(String... textArray)
    {
        if(textArray == null)
        {
            return true;
        }
        for(String text: textArray)
        {
            if (text == null)
            {
                continue; // null can not contain forbidden characters
            }
            if (!getCharacterCollection(text).stream().noneMatch(achar -> forbiddenCharacters.contains(achar)))
            {
                return false;
            }
        }
        return true;
    }

    public void addForbiddenCharacters(Collection<Character> forbiddenCharacters)
    {
        this.forbiddenCharacters.addAll(forbiddenCharacters);
    }

    private Collection<Character> getCharacterCollection(String text)
    {
        Collection<Character> characterCollection = new LinkedList<>();
        char[] chars = text.toLowerCase().toCharArray();
        for(char character: chars)
        {
            characterCollection.add(character);
        }
        return characterCollection;
    }

}
