package com.theplatform.commandbuilder.impl.build.handler;

import com.theplatform.commandbuilder.impl.command.api.ExternalCommand;

public interface HandlerCommandFactory
{
    ExternalCommand makeCommand(String... args);
}
