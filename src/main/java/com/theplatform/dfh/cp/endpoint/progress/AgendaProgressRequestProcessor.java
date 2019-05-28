package com.theplatform.dfh.cp.endpoint.progress;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaResponseReporter;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClientException;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.query.progress.ByAgendaProgressId;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;

import static com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaResponseReporter.AGENDA_RESPONSE_REPORTER_KEY;

/**
 * Agenda specific RequestProcessor
 */
public class AgendaProgressRequestProcessor extends EndpointDataObjectRequestProcessor<AgendaProgress>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectClient<OperationProgress> operationProgressClient;

    public AgendaProgressRequestProcessor(ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        super(agendaProgressPersister, new DataObjectValidator());

        operationProgressClient = new DataObjectRequestProcessorClient<>(new OperationProgressRequestProcessor(operationProgressPersister));
    }

    @Override
    public DataObjectResponse<AgendaProgress> handlePUT(DataObjectRequest<AgendaProgress> request) throws BadRequestException
    {
        AgendaProgress objectToUpdate = request.getDataObject();
        DataObjectResponse<AgendaProgress> response;
        objectToUpdate.setUpdatedTime(new Date());
        response = super.handlePUT(request);
        logAgenda(response);
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
                DataObjectResponse<OperationProgress> opProgressResponse = operationProgressClient.updateObject(op, op.getId());
                if (opProgressResponse.isError())
                    logger.error("Unable to update OperationProgress with id {}", op.getId(), opProgressResponse.getErrorResponse());
            }
        }
        return response;
    }

    private void logAgenda(DataObjectResponse<AgendaProgress> response)
    {
        AgendaProgress agendaProgress = response.getFirst();
        if(agendaProgress == null || agendaProgress.getParams() == null)
        {
            return;
        }
        if(agendaProgress.getParams().keySet().contains(AGENDA_RESPONSE_REPORTER_KEY) && agendaProgress.getParams().get(AGENDA_RESPONSE_REPORTER_KEY) instanceof AgendaResponseReporter)
        {
            AgendaResponseReporter agendaResponseReporter = (AgendaResponseReporter) agendaProgress.getParams().get(AGENDA_RESPONSE_REPORTER_KEY);
            agendaResponseReporter.setAgendaProgress(response.getFirst());
            agendaResponseReporter.reportAgendaResponse();
            agendaResponseReporter.reportAgendas();
        }
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
            agendaProgress.setOperationProgress(getOperationProgressObjects(agendaProgress.getId()));
        }
        return response;
    }

    private OperationProgress[] getOperationProgressObjects(String agendaProgressId)
    {
        ByAgendaProgressId byAgendaProgressId = new ByAgendaProgressId(agendaProgressId);
        try
        {
            DataObjectResponse<OperationProgress> opProgresses = operationProgressClient.getObjects(Collections.singletonList(byAgendaProgressId));
            return opProgresses.getAll().toArray(new OperationProgress[0]);
        }
        catch (ObjectClientException e)
        {
            logger.warn("Failed to retrieve OperationProgress objects. {}", e);
        }
        return null;
    }

    @Override
    public DataObjectResponse<AgendaProgress> handleDELETE(DataObjectRequest<AgendaProgress> request)
    {
        DataObjectResponse response = super.handleDELETE(request);

        // todo this will result in SO many calls.  Is there a better way to do this?
        // delete operationProgress objects
        OperationProgress[] operationProgressObjects = getOperationProgressObjects(request.getId());
        if(operationProgressObjects != null)
        {
            for (int i = 0; i < operationProgressObjects.length; i++)
            {
                operationProgressClient.deleteObject(operationProgressObjects[i].getId());
            }
        }
        return response;
    }
}
