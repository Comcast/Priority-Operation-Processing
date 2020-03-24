package com.theplatform.commandbuilder.impl.validate.rules;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RuleFactoryUtil
{
    // todo some of these chars may be in encoded strings that are legal; may need to refine security logic for params to accomodate that.
    private static List<Character> UNEXPECTED_FILE_PATH_CHARS_LIST =  "#%&{}[]<>*?,$!;@`|~^|".chars().mapToObj(c -> (char) c).collect(Collectors.toList());

    public static  final RuleNoForbiddenCharacters FORBIDDEN_CHARACTERS = new RuleNoForbiddenCharacters();

    static
    {
        FORBIDDEN_CHARACTERS.addForbiddenCharacters(UNEXPECTED_FILE_PATH_CHARS_LIST);
    }
}
