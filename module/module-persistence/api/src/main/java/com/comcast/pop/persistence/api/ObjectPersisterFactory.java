package com.comcast.pop.persistence.api;

import com.comcast.pop.object.api.IdentifiedObject;

/**
 * Basic factory interface for object persistence
 * @param <T>
 */
public interface ObjectPersisterFactory<T extends IdentifiedObject>
{
    ObjectPersister<T> getObjectPersister(String containerName);
}
