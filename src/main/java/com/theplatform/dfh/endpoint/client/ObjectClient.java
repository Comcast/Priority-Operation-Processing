package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public interface ObjectClient<T extends IdentifiedObject>
{
    DataObjectResponse<T> getObjects(String queryParams);
    DataObjectResponse<T> getObjects(List<Query> queries);
    DataObjectResponse<T> getObject(String id);
    DataObjectResponse<T> persistObject(T object);
    // TODO: id is not actually used by any implementation, remove from implementations
    DataObjectResponse<T> updateObject(T object, String id);
    DataObjectResponse<T> deleteObject(String id);
}
