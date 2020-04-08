package com.comcast.pop.commandbuilder.impl.command.api;

import com.comcast.pop.commandbuilder.impl.command.api.Phase.PhaseIntervalData;

public interface ExternalCommand extends Commandable, PhaseIntervalData
{
    String getProgramName();

    String toCommandString();

    String toScrubbedCommandString();
}
