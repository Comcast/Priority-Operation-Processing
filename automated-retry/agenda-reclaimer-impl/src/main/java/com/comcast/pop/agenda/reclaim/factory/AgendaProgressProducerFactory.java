package com.comcast.pop.agenda.reclaim.factory;

import com.comcast.pop.modules.sync.util.Producer;

public interface AgendaProgressProducerFactory
{
    Producer<String> create();
}
