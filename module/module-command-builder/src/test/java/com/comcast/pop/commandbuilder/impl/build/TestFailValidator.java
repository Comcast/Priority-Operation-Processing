package com.comcast.pop.commandbuilder.impl.build;

import com.comcast.pop.commandbuilder.api.CommandValidate;

import java.util.function.Predicate;

public class TestFailValidator implements CommandValidate
{

    @Override
    public boolean validate(String command) {
        return false;
    }

    @Override
    public void setRule(Predicate<String> rule) {

    }
}
