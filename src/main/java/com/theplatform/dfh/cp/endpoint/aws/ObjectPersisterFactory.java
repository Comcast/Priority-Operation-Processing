package com.theplatform.dfh.cp.endpoint.aws;

import com.theplatform.dfh.schedule.persistence.api.ObjectPersister;

/**
 * Basic factory interface for ObjectPersister
 * @param <T>
 */
public interface ObjectPersisterFactory<T>
{
    ObjectPersister<T> getObjectPersister();
}
