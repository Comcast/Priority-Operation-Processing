package com.comcast.pop.handler.puller.impl.executor;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;

public interface BaseLauncher
{
    void execute(Agenda agenda, AgendaProgress agendaProgress);
}