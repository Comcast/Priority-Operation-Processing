package com.theplatform.dfh.cp.endpoint.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaProgressReporter;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ObjectNotFoundException;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.ByFields;
import com.theplatform.dfh.endpoint.client.ObjectClientException;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.query.progress.ByAgendaProgressId;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public DataObjectResponse<AgendaProgress> handlePOST(DataObjectRequest<AgendaProgress> request)
    {
        AgendaProgress agendaProgress = request.getDataObject();
        // default vars
        if(agendaProgress != null)
        {
            if(agendaProgress.getMaximumAttempts() == null)
                agendaProgress.setMaximumAttempts(AgendaProgress.DEFAULT_MAX_ATTEMPTS);
        }
        return super.handlePOST(request);
    }

    @Override
    public DataObjectResponse<AgendaProgress> handlePUT(DataObjectRequest<AgendaProgress> request) throws BadRequestException
    {
        AgendaProgress objectToUpdate = request.getDataObject();
        // lookup the existing AgendaProgress (make sure they can access it) NOTE: This just looks up the AgendaProgress (not using the handleGET in this class)
        DefaultDataObjectRequest<AgendaProgress> agendaProgressLookup = new DefaultDataObjectRequest<>(null, objectToUpdate.getId(), null);
        agendaProgressLookup.setAuthorizationResponse(request.getAuthorizationResponse());
        DataObjectResponse<AgendaProgress> retrieveResponse = super.handleGET(agendaProgressLookup);

        if(retrieveResponse.isError())
            return retrieveResponse;
        AgendaProgress existingProgress = retrieveResponse.getFirst();
        if(existingProgress == null)
        {
            logger.warn("Unable to retrieve AgendaProgress: {}", request.getDataObject().getId());
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.objectNotFound(
                new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND_EXCEPTION, request.getDataObject().getId())), request.getCID()));
        }

        if(objectToUpdate.getOperationProgress() != null)
        {
            for (OperationProgress op : objectToUpdate.getOperationProgress())
            {
                op.setAgendaProgressId(existingProgress.getId());
                if (op.getId() == null)
                    op.setId(OperationProgress.generateId(existingProgress.getId(), op.getOperation()));
                DataObjectRequest<OperationProgress> opProgressReq = DefaultDataObjectRequest.customerAuthInstance(existingProgress.getCustomerId(), op);
                DataObjectResponse<OperationProgress> opProgressResponse = operationProgressClient.handlePUT(opProgressReq);
                if (opProgressResponse.isError())
                {
                    logger.error("Unable to update OperationProgress with id {} {}", op.getId(), opProgressResponse.getErrorResponse().toString());
                    return new DefaultDataObjectResponse<>(opProgressResponse.getErrorResponse());
                }
            }
        }

        updateAgendaProgressAttemptsOnComplete(objectToUpdate, existingProgress);
        DataObjectResponse<AgendaProgress> updateResponse = super.handlePUT(request);
        agendaProgressReporter.logCompletedAgenda(updateResponse);
        return updateResponse;
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
        boolean retrieveOperationProgress = shouldReturnField(request, "operationProgress");

        if(retrieveOperationProgress)
        {
            for (AgendaProgress agendaProgress : response.getAll())
            {
                DataObjectResponse<OperationProgress> opProgressResponse = getOperationProgressObjects(agendaProgress.getCustomerId(), agendaProgress.getId());
                // TODO: babs - tolerate the situation where there are no operation progress objects associated with an AgendaProgress (should not error)
                //AbstractServiceRequestProcessor.addErrorForObjectNotFound(opProgressResponse, OperationProgress.class, agendaProgress.getId(), request.getCID());
                if (opProgressResponse.isError())
                {
                    response.setErrorResponse(opProgressResponse.getErrorResponse());
                    logger.error(opProgressResponse.getErrorResponse().getServerStackTrace());
                    return response;
                }
                agendaProgress.setOperationProgress(opProgressResponse.getAll().toArray(new OperationProgress[0]));
            }
        }
        return response;
    }

    /**
     * Determines if a field should be returned based on the request
     * @param request The request to evaluate
     * @param fieldName The field to consider returning
     * @return True if there are no field queries or if the field is listed in a field query.
     */
    protected boolean shouldReturnField(DataObjectRequest<AgendaProgress> request, String fieldName)
    {
        if(request.getQueries() != null)
        {
            List<Query> fieldQueries = request.getQueries().stream().filter(q -> q.getField().isMatch(ByFields.fieldName())).collect(Collectors.toList());
            if(fieldQueries.size() > 0)
            {
                return fieldQueries.stream().anyMatch(q -> StringUtils.containsIgnoreCase(q.getStringValue(), fieldName));
            }
        }
        return true;
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
            logger.warn("Failed to retrieve OperationProgress objects.", e);
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.runtimeServiceException(e.getMessage(), null));
        }
    }

    protected void updateAgendaProgressAttemptsOnComplete(AgendaProgress updatedProgress, AgendaProgress originalProgress)
    {
        // confirm this is a flip from a incomplete -> complete AgendaProgress
        if(updatedProgress != null
            && originalProgress != null
            && updatedProgress.getProcessingState() == ProcessingState.COMPLETE
            && originalProgress.getProcessingState() != ProcessingState.COMPLETE)
        {
            // no regard for maximums or anything, just count it
            int currentAttemptCount = originalProgress.getAttemptsCompleted() == null ? 0 : originalProgress.getAttemptsCompleted();
            updatedProgress.setAttemptsCompleted(currentAttemptCount + 1);
        }
    }

    @Override
    public DataObjectResponse<AgendaProgress> handleDELETE(DataObjectRequest<AgendaProgress> request)
    {
        DataObjectResponse<AgendaProgress> response = super.handleDELETE(request);
        AgendaProgress agendaProgress = response.getFirst();

        // no AgendaProgress deleted, just return
        if(agendaProgress == null) return response;

        // todo this will result in SO many calls.  Is there a better way to do this?
        // delete operationProgress objects
        DataObjectResponse<OperationProgress> opProgressResponse;
        opProgressResponse = getOperationProgressObjects(agendaProgress.getCustomerId(), agendaProgress.getId());
        // TODO: babs - tolerate the situation where there are no operation progress objects associated with an AgendaProgress (should not error)
        //AbstractServiceRequestProcessor.addErrorForObjectNotFound(opProgressResponse, OperationProgress.class, agendaProgress.getId(), request.getCID());
        if(opProgressResponse.isError())
        {
            response.setErrorResponse(opProgressResponse.getErrorResponse());
            logger.error(opProgressResponse.getErrorResponse().getServerStackTrace());
            return response;
        }
        OperationProgress[] operationProgresses = opProgressResponse.getAll().toArray(new OperationProgress[0]);
        logger.debug("Found {} OperationProgress objects associated with AgendaProgress: {}", operationProgresses.length, agendaProgress.getId());
        for (int i = 0; i < operationProgresses.length; i++)
        {
            DataObjectRequest<OperationProgress> opProgressReq = DefaultDataObjectRequest.customerAuthInstance(agendaProgress.getCustomerId(), operationProgresses[i]);
            operationProgressClient.handleDELETE(opProgressReq);
        }
        return response;
    }
}
