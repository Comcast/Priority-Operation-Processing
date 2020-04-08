package com.comcast.pop.commandbuilder.impl.command.api;

import java.util.List;

public interface CommandFactory <T extends ExternalCommand>
{
    void makeCommands();
    List<T> getCommands();
}
