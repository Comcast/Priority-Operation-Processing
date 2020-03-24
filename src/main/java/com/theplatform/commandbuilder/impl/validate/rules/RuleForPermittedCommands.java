package com.theplatform.commandbuilder.impl.validate.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Rule is not testing for nulls.  If desired, null check should be its own rule.
 */
public class RuleForPermittedCommands implements Predicate<String>
{
    private Set<String> permittedCommandsSet = new HashSet<>();

    @Override
    public boolean test(String command)
    {
        if(command == null)
        {
            return false; // null can not contain permitted command
        }
        String commandLower = command.toLowerCase().trim();
        return permittedCommandsSet.stream().anyMatch(commandLower::startsWith);
    }

    public void addPermittedCommands(Collection<String> forbiddenCommands)
    {
        Collection<String> commandsLower = forbiddenCommands.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toList());
        permittedCommandsSet.addAll(commandsLower);
    }
}
