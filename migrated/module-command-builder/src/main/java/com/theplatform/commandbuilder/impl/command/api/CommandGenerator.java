package com.theplatform.commandbuilder.impl.command.api;

import com.theplatform.commandbuilder.impl.command.api.ExternalCommand;

import java.util.List;

public interface CommandGenerator
{
    List<ExternalCommand> generateCommands();
}
