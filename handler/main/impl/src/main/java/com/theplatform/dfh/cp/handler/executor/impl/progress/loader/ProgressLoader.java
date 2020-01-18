package com.theplatform.dfh.cp.handler.executor.impl.progress.loader;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;

public interface ProgressLoader
{
    AgendaProgress loadProgress(String agendaProgressId);
}
