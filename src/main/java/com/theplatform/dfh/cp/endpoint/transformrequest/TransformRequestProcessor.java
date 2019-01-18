package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.transformrequest.agenda.generator.PrepOpsGenerator;
import com.theplatform.dfh.cp.endpoint.validation.TransformValidator;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * TransformRequest specific RequestProcessor
 */
public class TransformRequestProcessor extends DataObjectRequestProcessor<TransformRequest>
{
    private static final Logger logger = LoggerFactory.getLogger(TransformRequestProcessor.class);
    private JsonHelper jsonHelper = new JsonHelper();

    private PrepOpsGenerator prepOpsGenerator;
    private ObjectClient<AgendaProgress> agendaProgressClient;
    private ObjectClient<Agenda> agendaClient;

    public TransformRequestProcessor(
        ObjectPersister<TransformRequest> transformRequestObjectPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        super(transformRequestObjectPersister, new TransformValidator());
        prepOpsGenerator = new PrepOpsGenerator();
        agendaProgressClient = new DataObjectRequestProcessorClient<>(new AgendaProgressRequestProcessor(agendaProgressPersister, operationProgressPersister));
        agendaClient = new DataObjectRequestProcessorClient<>(new AgendaRequestProcessor(
            agendaPersister,
            agendaProgressPersister,
            readyAgendaPersister,
            operationProgressPersister,
            insightPersister,
            customerPersister
        ));
    }

    @Override
    public DataObjectResponse<TransformRequest> handlePOST(DataObjectRequest<TransformRequest> request) throws BadRequestException
    {
        //We create the transform req first, so we have the ID for progress operations.
        //If progress fails, we need to rollback the transformReq.
        DataObjectResponse<TransformRequest> response = super.handlePOST(request);
        TransformRequest transformRequest = response.getFirst();

        // TODO: this needs to clean up the objects on fail
        ////
        // persist the prep/exec progress
        ////
        AgendaProgress prepAgendaProgressResponse = createAgendaProgress(transformRequest.getId(), transformRequest.getExternalId(), transformRequest.getCustomerId());
        AgendaProgress execAgendaProgressResponse = createAgendaProgress(transformRequest.getId(), transformRequest.getExternalId(), transformRequest.getCustomerId());

        if(transformRequest.getParams() == null) transformRequest.setParams(new ParamsMap());
        //This information is used for prep agenda generation, maybe these don't belong on the transform request itself
        transformRequest.getParams().put(GeneralParamKey.progressId, prepAgendaProgressResponse.getId());
        transformRequest.getParams().put(GeneralParamKey.execProgressId, execAgendaProgressResponse.getId());

        String agendaId = createAgenda(transformRequest, prepAgendaProgressResponse);
        transformRequest.getParams().put(GeneralParamKey.agendaId, agendaId);

        return response;
    }

    private String createAgenda(TransformRequest request, AgendaProgress prepAgendaProgressResponse)
    {
        ////
        // persist the prepAgenda (this is intentionally last as the Agenda may begin processing immediately)
        ////
        Agenda prepAgenda = prepOpsGenerator.generateAgenda(request, prepAgendaProgressResponse.getId());

        //logger.debug("Generated Agenda: {}", jsonHelper.getJSONString(agenda));
        DataObjectResponse<Agenda> prepAgendaResponse;
        try
        {
            prepAgendaResponse = agendaClient.persistObject(prepAgenda);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to create connection to persist the Agenda generated from the TransformRequest.", e);
        }

        if(prepAgendaResponse.isError() || prepAgendaResponse.getFirst() == null)
        {
            throw new RuntimeException("Failed to create prep Agenda.", prepAgendaResponse.getException());
        }
        return prepAgendaResponse.getFirst().getId();
    }

    private AgendaProgress createAgendaProgress(String transformRequestId, String externalId, String customerId)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        // NOTE: link id is the transformRequest id
        agendaProgress.setCustomerId(customerId);
        agendaProgress.setLinkId(transformRequestId);
        agendaProgress.setExternalId(externalId);
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        logger.debug("Generated AgendaProgress: {}", jsonHelper.getJSONString(agendaProgress));
        try
        {
            DataObjectResponse<AgendaProgress> response = agendaProgressClient.persistObject(agendaProgress);
            if(response.isError()) throw response.getException();
            return response.getFirst();
        }
        catch(Exception e)
        {
            throw new RuntimeException(String.format("Failed to persist the Progress TransformRequest: %1$s", transformRequestId), e);
        }
    }

    @Override
    public RequestValidator<DataObjectRequest<TransformRequest>> getRequestValidator()
    {
        return new TransformValidator();
    }

    public void setPrepOpsGenerator(PrepOpsGenerator prepOpsGenerator)
    {
        this.prepOpsGenerator = prepOpsGenerator;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setAgendaProgressClient(ObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    public void setAgendaClient(ObjectClient<Agenda> agendaClient)
    {
        this.agendaClient = agendaClient;
    }
}
