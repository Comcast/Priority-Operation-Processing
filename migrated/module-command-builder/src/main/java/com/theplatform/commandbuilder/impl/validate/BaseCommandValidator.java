package com.theplatform.commandbuilder.impl.validate;

import com.theplatform.commandbuilder.api.CommandValidate;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class BaseCommandValidator implements CommandValidate {

    private List<Predicate<String>> rules = new LinkedList<>();


    @Override
    public boolean validate(String command)
    {
        return rules.stream().allMatch(rule -> rule.test(command));
    }

    @Override
    public void setRule(Predicate<String> rule)
    {
        rules.add(rule);
    }

}
