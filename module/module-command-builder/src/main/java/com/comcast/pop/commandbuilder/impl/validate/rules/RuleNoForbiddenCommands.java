package com.comcast.pop.commandbuilder.impl.validate.rules;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Rule is not testing for nulls.  If desired, null check should be its own rule.
 */
public class RuleNoForbiddenCommands implements Predicate<String> {

    private Set<String> forbiddenCommandsSet = new HashSet<>();

    @Override
    public boolean test(String command)
    {
        if(command == null)
        {
            return true; // null can not contain forbidden command
        }
        String commandLower = command.toLowerCase();
        return forbiddenCommandsSet.stream().noneMatch(commandLower::contains);
    }

    public void addForbiddenCommands(Collection<String> forbiddenCommands)
    {
        Collection<String> commandsLower = forbiddenCommands.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toList());
        forbiddenCommandsSet.addAll(commandsLower);
    }

}
