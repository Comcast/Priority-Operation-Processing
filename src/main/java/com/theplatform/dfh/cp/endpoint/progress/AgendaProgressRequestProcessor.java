package com.theplatform.dfh.cp.endpoint.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaProgressReporter;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClientException;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.query.progress.ByAgendaProgressId;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Agenda specific RequestProcessor
 */
public class AgendaProgressRequestProcessor extends EndpointDataObjectRequestProcessor<AgendaProgress>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private OperationProgressRequestProcessor operationProgressClient;
    private final AgendaProgressReporter agendaProgressReporter;

    public AgendaProgressRequestProcessor(ObjectPersister<AgendaProgress> agendaProgressPersister, ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        super(agendaProgressPersister, new DataObjectValidator<>());
        agendaProgressReporter = new AgendaProgressReporter(agendaPersister);
        agendaProgressReporter.setAgendaProgressPersister(agendaProgressPersister);
        operationProgressClient = new OperationProgressRequestProcessor(operationProgressPersister);
    }

    @Override
    public DataObjectResponse<AgendaProgress> handlePUT(DataObjectRequest<AgendaProgress> request) throws BadRequestException
    {
        AgendaProgress objectToUpdate = request.getDataObject();
        DataObjectResponse<AgendaProgress> response;
        response = super.handlePUT(request);

        agendaProgressReporter.logCompletedAgenda(response);
        if(response.isError())
            return response;
        //@todo tstair -- We post progress before agenda in POST but here we do it after????
        if(objectToUpdate.getOperationProgress() != null)
        {
            for (OperationProgress op : objectToUpdate.getOperationProgress())
            {
                op.setAgendaProgressId(objectToUpdate.getId());
                if (op.getId() == null)
                    op.setId(OperationProgress.generateId(objectToUpdate.getId(), op.getOperation()));
                DataObjectRequest<OperationProgress> opProgressReq = DefaultDataObjectRequest.customerAuthInstance(objectToUpdate.getCustomerId(), op);
                DataObjectResponse<OperationProgress> opProgressResponse = operationProgressClient.handlePUT(opProgressReq);
                if (opProgressResponse.isError())
                    logger.error("Unable to update OperationProgress with id {}", op.getId(), opProgressResponse.getErrorResponse());
            }
        }
        return response;
    }

    /**
     * Handles the GET of an object
     * @param request The request to operate with
     * @return The object, or null if not found
     */
    @Override
    public DataObjectResponse<AgendaProgress> handleGET(DataObjectRequest<AgendaProgress> request)
    {
        DataObjectResponse<AgendaProgress> response = super.handleGET(request);
        for (AgendaProgress agendaProgress : response.getAll())
        {
            DataObjectResponse<OperationProgress> opProgressResponse = getOperationProgressObjects(agendaProgress.getCustomerId(), agendaProgress.getId());
            AbstractServiceRequestProcessor.addErrorForObjectNotFound(opProgressResponse, OperationProgress.class, agendaProgress.getId(), request.getCID());
            if(opProgressResponse.isError())
            {
                response.setErrorResponse(opProgressResponse.getErrorResponse());
                logger.error(opProgressResponse.getErrorResponse().getServerStackTrace());
                return response;
            }
            agendaProgress.setOperationProgress(opProgressResponse.getAll().toArray(new OperationProgress[0]));
        }
        return response;
    }

    private DataObjectResponse<OperationProgress> getOperationProgressObjects(String customerID, String agendaProgressId)
    {
        ByAgendaProgressId byAgendaProgressId = new ByAgendaProgressId(agendaProgressId);
        try
        {
            DataObjectRequest<OperationProgress> opProgressReq = DefaultDataObjectRequest.customerAuthInstance(customerID, null);
            opProgressReq.setQueries(Collections.singletonList(byAgendaProgressId));
            return operationProgressClient.handleGET(opProgressReq);
        }
        catch (ObjectClientException e)
        {
            logger.warn("Failed to retrieve OperationProgress objects. {}", e);
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.runtimeServiceException(e.getMessage(), null));
        }
    }

    @Override
    public DataObjectResponse<AgendaProgress> handleDELETE(DataObjectRequest<AgendaProgress> request)
    {
        DataObjectResponse<AgendaProgress> response = super.handleDELETE(request);
        AgendaProgress agendaProgress = request.getDataObject();

        // todo this will result in SO many calls.  Is there a better way to do this?
        // delete operationProgress objects
        DataObjectResponse<OperationProgress> opProgressResponse = getOperationProgressObjects(agendaProgress.getCustomerId(), agendaProgress.getId());
        AbstractServiceRequestProcessor.addErrorForObjectNotFound(opProgressResponse, OperationProgress.class, agendaProgress.getId(), request.getCID());
        if(opProgressResponse.isError())
        {
            response.setErrorResponse(opProgressResponse.getErrorResponse());
            logger.error(opProgressResponse.getErrorResponse().getServerStackTrace());
            return response;
        }
        OperationProgress[] operationProgresses = opProgressResponse.getAll().toArray(new OperationProgress[0]);
        if(operationProgresses != null)
        {
            for (int i = 0; i < operationProgresses.length; i++)
            {
                DataObjectRequest<OperationProgress> opProgressReq = DefaultDataObjectRequest.customerAuthInstance(agendaProgress.getCustomerId(), operationProgresses[i]);
                operationProgressClient.handleDELETE(opProgressReq);
            }
        }
        return response;
    }
}
