package com.comcast.pop.commandbuilder.api;

import java.util.function.Predicate;

public interface CommandValidate
{
    boolean validate(String command);

    void setRule(Predicate<String> rule);

}
