package com.theplatform.commandbuilder.impl.command.api;

import com.theplatform.commandbuilder.impl.command.api.Phase.PhaseInterval;

import java.util.List;

public interface CommandList< T extends ExternalCommand>
{
    void add(T command, PhaseInterval phaseInterval);

    void add(ProgressFactory<T> progressFactory);

    void clear();

    List<T> get();
}
