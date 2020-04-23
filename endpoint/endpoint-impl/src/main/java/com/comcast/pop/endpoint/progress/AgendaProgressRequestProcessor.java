package com.comcast.pop.endpoint.progress;

import com.comcast.pop.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.comcast.pop.endpoint.validation.AgendaProgressValidator;
import com.comcast.pop.endpoint.api.ErrorResponseCode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.agenda.reporter.AgendaProgressReporter;
import com.comcast.pop.endpoint.base.EndpointDataObjectRequestProcessor;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.ObjectNotFoundException;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.endpoint.api.data.query.ByFields;
import com.comcast.pop.endpoint.client.ObjectClientException;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.data.query.progress.ByAgendaProgressId;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.query.Query;
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
        super(agendaProgressPersister, new AgendaProgressValidator());
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
        AgendaProgress retrievedProgress = retrieveResponse.getFirst();
        if(retrievedProgress == null)
        {
            logger.warn("Unable to retrieve AgendaProgress: {}", request.getDataObject().getId());
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.objectNotFound(
                new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND_EXCEPTION, request.getDataObject().getId())), request.getCID()));
        }

        if(objectToUpdate.getOperationProgress() != null)
        {
            for (OperationProgress op : objectToUpdate.getOperationProgress())
            {
                if(op == null) continue; // help the user a slight bit

                op.setAgendaProgressId(retrievedProgress.getId());
                if (op.getId() == null)
                    op.setId(OperationProgress.generateId(retrievedProgress.getId(), op.getOperation()));
                DataObjectRequest<OperationProgress> opProgressReq = DefaultDataObjectRequest.customerAuthInstance(retrievedProgress.getCustomerId(), op);
                DataObjectResponse<OperationProgress> opProgressResponse = operationProgressClient.handlePUT(opProgressReq);

                // an op progress can be created on-the-fly, if the update fails attempt a create
                // TODO: consider a param on the op progress indicating this is a generated op progress
                if (opProgressResponse.isError()
                    && ErrorResponseCode.getFromCode(opProgressResponse.getErrorResponse().getResponseCode()) == ErrorResponseCode.OBJECT_NOT_FOUND)
                {
                    opProgressReq = DefaultDataObjectRequest.customerAuthInstance(retrievedProgress.getCustomerId(), populateOperationProgressForCreate(retrievedProgress, op));
                    opProgressResponse = operationProgressClient.handlePOST(opProgressReq);
                }
                // PUT/POST may result in an error
                if (opProgressResponse.isError())
                {
                    logger.error("Unable to update OperationProgress with id {} {}", op.getId(), opProgressResponse.getErrorResponse().toString());
                    return new DefaultDataObjectResponse<>(opProgressResponse.getErrorResponse());
                }
            }
        }

        updateAgendaProgressAttemptsOnComplete(objectToUpdate, retrievedProgress);
        DataObjectResponse<AgendaProgress> updateResponse = super.handlePUT(request, retrievedProgress);
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
                // TODO - tolerate the situation where there are no operation progress objects associated with an AgendaProgress (should not error)
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
        // TODO: tolerate the situation where there are no operation progress objects associated with an AgendaProgress (should not error)
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

    protected OperationProgress populateOperationProgressForCreate(AgendaProgress agendaProgress, OperationProgress sourceProgress)
    {
        sourceProgress.setCustomerId(agendaProgress.getCustomerId());
        sourceProgress.setAgendaProgressId(agendaProgress.getId());
        sourceProgress.setProcessingState(sourceProgress.getProcessingState() == null ? ProcessingState.WAITING : sourceProgress.getProcessingState());
        sourceProgress.setCid(agendaProgress.getCid());
        sourceProgress.setId(OperationProgress.generateId(agendaProgress.getId(), sourceProgress.getOperation()));
        return sourceProgress;
    }

    public void setOperationProgressClient(OperationProgressRequestProcessor operationProgressClient)
    {
        this.operationProgressClient = operationProgressClient;
    }
}
