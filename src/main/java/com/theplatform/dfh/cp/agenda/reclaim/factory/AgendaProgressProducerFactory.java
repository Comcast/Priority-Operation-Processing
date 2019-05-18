package com.theplatform.dfh.cp.agenda.reclaim.factory;

import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;

public interface AgendaProgressProducerFactory<T extends ReclaimerConfig>
{
    Producer<String> create(T config);
}
