package com.theplatform.dfh.cp.handler.puller.impl.executor;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;

public interface BaseLauncher
{
    void execute(Agenda agenda, AgendaProgress agendaProgress);
}