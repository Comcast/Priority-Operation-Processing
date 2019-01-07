package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public interface ObjectClient<T>
{
    DataObjectFeed<T> getObjects(String queryParams);
    DataObjectFeed<T> getObjects(List<Query> queries);
    T getObject(String id);
    ObjectPersistResponse persistObject(T object);
    // TODO: id is not actually used by any implementation, remove from implementations
    void updateObject(T object, String id);
    void deleteObject(String id);
}
