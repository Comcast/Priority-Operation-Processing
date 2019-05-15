package com.theplatform.dfh.cp.agenda.reclaim.factory;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;

public interface AgendaProgressConsumerFactory
{
    Consumer<AgendaProgress> create(ReclaimerConfig config);
}
