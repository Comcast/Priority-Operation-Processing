package com.theplatform.dfh.cp.endpoint.cleanup;

import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersisterObjectTracker<T extends IdentifiedObject> extends ObjectTracker<T>
{
    private static final Logger logger = LoggerFactory.getLogger(PersisterObjectTracker.class);

    private ObjectPersister<T> objectPersister;

    public PersisterObjectTracker(ObjectPersister<T> objectPersister, Class<T> clazz)
    {
        super(clazz);
        this.objectPersister = objectPersister;
    }

    @Override
    public void cleanUp()
    {
        for (String id : getObjectIds())
        {
            try
            {
                objectPersister.delete(id);
            }
            catch (PersistenceException e)
            {
                logger.error("Failed to delete {} with id {}", getObjectClass().getSimpleName(), id, e);
            }
        }
    }
}
