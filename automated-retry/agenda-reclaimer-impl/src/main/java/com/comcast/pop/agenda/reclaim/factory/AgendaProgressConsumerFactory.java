package com.comcast.pop.agenda.reclaim.factory;

import com.comcast.pop.modules.sync.util.Consumer;

public interface AgendaProgressConsumerFactory
{
    Consumer<String> create();
}
