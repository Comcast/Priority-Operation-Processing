package com.comcast.pop.commandbuilder.impl.command.api;

import com.comcast.pop.commandbuilder.impl.command.api.Phase.PhaseInterval;

import java.util.List;

public interface ProgressFactory<T extends ExternalCommand> extends CommandFactory<T>
{
    List<PhaseInterval> getProgressData();
}
