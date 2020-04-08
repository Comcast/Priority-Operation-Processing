package com.comcast.pop.commandbuilder.impl.build.handler;

import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;

public interface HandlerCommandFactory
{
    ExternalCommand makeCommand(String... args);
}
