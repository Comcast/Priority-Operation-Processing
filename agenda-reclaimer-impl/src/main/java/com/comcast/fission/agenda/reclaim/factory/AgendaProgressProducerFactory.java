package com.comcast.fission.agenda.reclaim.factory;

import com.theplatform.com.dfh.modules.sync.util.Producer;

public interface AgendaProgressProducerFactory
{
    Producer<String> create();
}
