package com.theplatform.commandbuilder.impl.validate;

import com.theplatform.commandbuilder.api.CommandValidate;

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
