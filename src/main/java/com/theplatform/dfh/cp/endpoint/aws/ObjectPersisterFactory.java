package com.theplatform.dfh.cp.endpoint.aws;

import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * Basic factory interface for ObjectPersister
 * @param <T>
 */
public interface ObjectPersisterFactory<T>
{
    ObjectPersister<T> getObjectPersister(String containerName);
}
