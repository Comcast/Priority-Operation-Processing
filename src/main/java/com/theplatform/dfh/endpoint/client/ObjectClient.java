package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public interface ObjectClient<T>
{
    DataObjectResponse getObjects(String queryParams);
    DataObjectResponse getObjects(List<Query> queries);
    T getObject(String id);
    T persistObject(T object);
    // TODO: id is not actually used by any implementation, remove from implementations
    T updateObject(T object, String id);
    void deleteObject(String id);
}
