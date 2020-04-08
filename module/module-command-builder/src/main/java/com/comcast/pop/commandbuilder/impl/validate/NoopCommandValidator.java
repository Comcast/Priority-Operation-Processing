package com.comcast.pop.commandbuilder.impl.validate;

import com.comcast.pop.commandbuilder.api.CommandValidate;

import java.util.function.Predicate;

public class NoopCommandValidator implements CommandValidate
{

    @Override
    public boolean validate(String command)
    {
        return true;
    }

    @Override
    public void setRule(Predicate<String> rule)
    {

    }
}
