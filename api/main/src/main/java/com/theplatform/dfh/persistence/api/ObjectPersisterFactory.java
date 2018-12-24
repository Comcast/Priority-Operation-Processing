package com.theplatform.dfh.persistence.api;

import com.theplatform.dfh.object.api.IdentifiedObject;

/**
 * Basic factory interface for object persistence
 * @param <T>
 */
public interface ObjectPersisterFactory<T extends IdentifiedObject>
{
    ObjectPersister<T> getObjectPersister(String containerName);
}
