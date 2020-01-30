package com.theplatform.dfh.cp.endpoint.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class RequestProcessorFactory
{
    public AgendaRequestProcessor createAgendaRequestProcessor(ObjectPersister<Agenda> agendaRequestPersister,
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        return new AgendaRequestProcessor(agendaRequestPersister, agendaProgressPersister, readyAgendaPersister,
            operationProgressPersister, insightPersister, customerPersister);
    }

    public AgendaProgressRequestProcessor createAgendaProgressRequestProcessor(ObjectPersister<AgendaProgress> agendaProgressPersister, ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        return new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
    }

    public OperationProgressRequestProcessor createOperationProgressRequestProcessor(ObjectPersister<OperationProgress> operationProgressPersister)
    {
        return new OperationProgressRequestProcessor(operationProgressPersister);
    }
}
