package com.comcast.pop.test.cleanup.endpoint;

import com.comcast.pop.endpoint.client.HttpObjectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class IdentifiedObjectCleanupManager
{
    private static final Logger logger = LoggerFactory.getLogger(IdentifiedObjectCleanupManager.class);

    private final IdentifiedObjectCreateTracker identifiedObjectCreateTracker;
    private Map<Class, HttpObjectClient> objectClientMap;

    public IdentifiedObjectCleanupManager(IdentifiedObjectCreateTracker identifiedObjectCreateTracker, List<HttpObjectClient> objectClients)
    {
        this.identifiedObjectCreateTracker = identifiedObjectCreateTracker;
        objectClientMap = objectClients.stream().collect(Collectors.toMap(HttpObjectClient::getObjectClass, client -> client));
    }

    /**
     * Remaps the cleanup object client associated with the type to the client specified
     * @param objectClient The client to use for object clean up
     */
    public void setupObjectCleanupClient(HttpObjectClient objectClient)
    {
        objectClientMap.put(objectClient.getObjectClass(), objectClient);
    }

    public void clean()
    {
        for(Map.Entry<Class, Set<String>> entry : identifiedObjectCreateTracker.getClassObjectIdMap().entrySet())
        {
            HttpObjectClient objectClient = objectClientMap.get(entry.getKey());
            Set<String> idsToDelete = entry.getValue();
            for(String id : idsToDelete)
            {
                try
                {
                    objectClient.deleteObject(id);
                    logger.debug("Deleted object {} - {} ", objectClient.getObjectClass().getSimpleName(), id);
                }
                catch(Throwable e)
                {
                    logger.error(String.format("Failed to clean up object %1$s - %2$s ", objectClient.getObjectClass().getSimpleName(), id), e);
                }
            }
            idsToDelete.clear();
        }
    }

    public IdentifiedObjectCreateTracker getIdentifiedObjectCreateTracker()
    {
        return identifiedObjectCreateTracker;
    }
}
