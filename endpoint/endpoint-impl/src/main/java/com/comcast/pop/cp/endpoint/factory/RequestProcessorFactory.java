package com.comcast.pop.cp.endpoint.factory;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.cp.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.comcast.pop.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.comcast.pop.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.cp.endpoint.resourcepool.CustomerRequestProcessor;
import com.comcast.pop.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.comcast.pop.cp.endpoint.resourcepool.insight.mapper.InsightSelector;
import com.comcast.pop.endpoint.base.DataObjectRequestProcessor;
import com.comcast.pop.endpoint.base.visibility.AllMatchVisibilityFilter;
import com.comcast.pop.endpoint.base.visibility.ServiceRequestVisibilityFilter;
import com.comcast.pop.endpoint.base.visibility.VisibilityMethod;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.persistence.api.ObjectPersister;

/**
 * RequestProcessorFactory (great for unit testing!) Enjoy.
 */
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

    public AgendaRequestProcessor createAgendaRequestProcessor(ObjectPersister<Agenda> agendaRequestObjectPersister,
        ObjectPersister<ReadyAgenda> readyAgendaObjectPersister,
        DataObjectRequestProcessor<AgendaProgress> agendaProgressRequestProcessor,
        DataObjectRequestProcessor<OperationProgress> operationProgressRequestProcessor,
        InsightSelector insightSelector)
    {
        return new AgendaRequestProcessor(agendaRequestObjectPersister, readyAgendaObjectPersister, agendaProgressRequestProcessor,
            operationProgressRequestProcessor, insightSelector);
    }

    /**
     * Creates an AgendaRequestProcessor with an InsightRequestProcessor with Service level visibility
     * (see createInsightRequestProcessorWithServiceRequestVisility).
     */
    public AgendaRequestProcessor createAgendaRequestProcessorWithServiceRequestVisibility(ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister,
        ServiceRequest serviceRequest)
    {
        InsightRequestProcessor insightRequestProcessor =
            createInsightRequestProcessorWithServiceRequestVisibility(insightPersister, serviceRequest);

        CustomerRequestProcessor customerRequestProcessor = new CustomerRequestProcessor(customerPersister);
        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        OperationProgressRequestProcessor operationProgressRequestProcessor = new OperationProgressRequestProcessor(operationProgressPersister);
        return createAgendaRequestProcessor(agendaPersister,
            readyAgendaPersister,
            agendaProgressRequestProcessor,
            operationProgressRequestProcessor,
            new InsightSelector(insightRequestProcessor, customerRequestProcessor));
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

    public AgendaTemplateRequestProcessor createAgendaTemplateRequestProcessor(ObjectPersister<AgendaTemplate> agendaTemplatePersister)
    {
        return new AgendaTemplateRequestProcessor(agendaTemplatePersister);
    }

    public InsightRequestProcessor createInsightRequestProcessor(ObjectPersister<Insight> insightObjectPersister)
    {
        return new InsightRequestProcessor(insightObjectPersister);
    }

    public InsightRequestProcessor createInsightRequestProcessorWithServiceRequestVisibility(ObjectPersister<Insight> insightObjectPersister,
        ServiceRequest serviceRequest)
    {
        InsightRequestProcessor processor = new InsightRequestProcessor(insightObjectPersister);
        //the service needs extra visibility checking.
        //get current visibilty filter
        AllMatchVisibilityFilter allMatchVisibilityFilter = new AllMatchVisibilityFilter<>()
            .withFilter(InsightRequestProcessor.getDefaultObjectReadVisibilityFilter())
            .withFilter(new ServiceRequestVisibilityFilter<>(serviceRequest));
        processor.setVisibilityFilter(VisibilityMethod.GET, allMatchVisibilityFilter);
        return processor;
    }
}
