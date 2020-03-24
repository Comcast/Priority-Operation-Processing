package com.theplatform.dfh.cp.endpoint.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.visibility.AllMatchVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.ServiceRequestVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityMethod;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.CustomerRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;

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
