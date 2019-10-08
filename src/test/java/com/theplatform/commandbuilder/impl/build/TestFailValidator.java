package com.theplatform.commandbuilder.impl.build;

import com.theplatform.commandbuilder.api.CommandValidate;

import java.util.function.Predicate;

public class TestFailValidator implements CommandValidate {

    @Override
    public boolean validate(String command) {
        return false;
    }

    @Override
    public void setRule(Predicate<String> rule) {

    }
}
