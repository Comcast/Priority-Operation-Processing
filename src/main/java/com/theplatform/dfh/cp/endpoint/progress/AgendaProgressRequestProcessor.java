package com.theplatform.dfh.cp.endpoint.progress;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.adapter.client.RequestProcessorAdapter;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.endpoint.client.ObjectClientException;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.endpoint.api.query.progress.ByAgendaProgressId;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agenda specific RequestProcessor
 */
public class AgendaProgressRequestProcessor extends BaseRequestProcessor<AgendaProgress>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectClient<OperationProgress> operationProgressClient;

    public AgendaProgressRequestProcessor(ObjectPersister<AgendaProgress> agendaRequestPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        super(agendaRequestPersister);

        operationProgressClient = new RequestProcessorAdapter<>(new OperationProgressRequestProcessor(operationProgressPersister));
    }

//    @Override
//    public ObjectPersistResponse handlePOST(AgendaProgress objectToPersist) throws BadRequestException
//    {
//        if (objectToPersist.getId() != null || objectToPersist.getId().length() != 0)
//        {
//            throw new BadRequestException("Id specification not supported.");
//        }
//
//    }


    @Override
    public void handlePUT(AgendaProgress objectToUpdate) throws BadRequestException
    {
        try
        {
            objectToUpdate.setUpdatedTime(new Date());
            objectPersister.update(objectToUpdate);
            if(objectToUpdate.getOperationProgress() != null)
            {
                for (OperationProgress op : objectToUpdate.getOperationProgress())
                {
                    try
                    {
                        op.setAgendaProgressId(objectToUpdate.getId());
                        if (op.getId() == null)
                            op.setId(OperationProgress.generateId(objectToUpdate.getId(), op.getOperation()));
                        operationProgressClient.updateObject(op, op.getId());
                    }
                    catch (Exception e)
                    {
                        logger.error("Unable to update OperationProgress with id {}", op.getId(), e);
                    }
                }
            }
        }
        catch(PersistenceException e)
        {
            final String id = objectToUpdate == null ? "UNKNOWN" : objectToUpdate.getId();
            throw new BadRequestException(String.format("Unable to update object by id {}", id), e);
        }
    }

    /**
     * Handles the GET of an object
     * @param id id of the object to get
     * @return The object, or null if not found
     */
    public AgendaProgress handleGET(String id)
    {
        AgendaProgress agendaProgress;
        try
        {
            agendaProgress = objectPersister.retrieve(id);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to get object by id {}", id), e);
        }

        if(agendaProgress != null)
        {
            agendaProgress.setOperationProgress(getOperationProgressObjects(agendaProgress.getId()));
        }
        return agendaProgress;
    }
    /**
     * Handles the GET of an object
     * @param queries Queries by fields
     * @return The object, or null if not found
     */
    public DataObjectFeed<AgendaProgress> handleGET(List<Query> queries)
    {
        DataObjectFeed<AgendaProgress> agendaProgressFeed;
        try
        {
            agendaProgressFeed = objectPersister.retrieve(queries);
        }
        catch(PersistenceException e)
        {
            final String queryString = queries.stream().map( Object::toString ).collect( Collectors.joining( "," ) );
            throw new BadRequestException(String.format("Unable to get object by queries {}", queryString), e);
        }

        for (AgendaProgress agendaProgress : agendaProgressFeed.getAll())
        {
            agendaProgress.setOperationProgress(getOperationProgressObjects(agendaProgress.getId()));
        }
        return agendaProgressFeed;
    }

    private OperationProgress[] getOperationProgressObjects(String agendaProgressId)
    {
        ByAgendaProgressId byAgendaProgressId = new ByAgendaProgressId(agendaProgressId);
        try
        {
            DataObjectFeed<OperationProgress> opProgresses = operationProgressClient.getObjects(Collections.singletonList(byAgendaProgressId));
            return opProgresses.getAll().toArray(new OperationProgress[0]);
        }
        catch (ObjectClientException e)
        {
            logger.warn("Failed to retrieve OperationProgress objects. {}", e);
        }
        return null;
    }

    /**
     * Handles the DELETE of an object
     * @param id The id of the object to delete
     */
    public void handleDelete(String id) {
        try
        {
            objectPersister.delete(id);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to delete object by id {}", id), e);
        }

        // todo this will result in SO many calls.  Is there a better way to do this?
        // delete operationProgress objects
        OperationProgress[] operationProgressObjects = getOperationProgressObjects(id);
        for (int i = 0; i < operationProgressObjects.length; i++)
        {
            operationProgressClient.deleteObject(operationProgressObjects[i].getId());
        }
    }
}
