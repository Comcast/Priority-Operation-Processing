package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.factory.AgendaExecutorFactory;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;

public class OperationContext extends BaseOperationContext
{
    private AgendaExecutorFactory agendaExecutorFactory;

    public OperationContext(AgendaExecutorFactory agendaExecutorFactory, Reporter reporter)
    {
        super(reporter);
        this.agendaExecutorFactory = agendaExecutorFactory;
    }

    public AgendaExecutorFactory getAgendaExecutorFactory()
    {
        return agendaExecutorFactory;
    }

    public void setAgendaExecutorFactory(AgendaExecutorFactory agendaExecutorFactory)
    {
        this.agendaExecutorFactory = agendaExecutorFactory;
    }
}
