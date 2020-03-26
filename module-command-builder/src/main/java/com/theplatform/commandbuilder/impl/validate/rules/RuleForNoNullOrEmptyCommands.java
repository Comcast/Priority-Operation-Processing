package com.theplatform.commandbuilder.impl.validate.rules;


import java.util.function.Predicate;

public class RuleForNoNullOrEmptyCommands  implements Predicate<String>
{

    @Override
    public boolean test(String command)
    {
        return stringIsNotNullOrEmplty(command);
    }

    private boolean stringIsNotNullOrEmplty(String command) {
        return command != null && !command.trim().isEmpty();
    }
}
