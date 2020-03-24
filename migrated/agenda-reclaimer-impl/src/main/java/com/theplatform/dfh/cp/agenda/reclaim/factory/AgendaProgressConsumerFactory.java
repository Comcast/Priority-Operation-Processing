package com.theplatform.dfh.cp.agenda.reclaim.factory;

import com.theplatform.com.dfh.modules.sync.util.Consumer;

public interface AgendaProgressConsumerFactory
{
    Consumer<String> create();
}
