package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.api.EndpointDataObject;
import com.theplatform.dfh.cp.endpoint.base.persistence.EndpointDataObjectPersister;
import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * EndpointDataObject specific request processor. This wraps the incoming object persister.
 *
 * This is necessary because not all our objects are EndpointDataObjects with time fields.
 *
 * @param <T> an EndpointDataObject type
 */
public class EndpointDataObjectRequestProcessor<T extends EndpointDataObject> extends DataObjectRequestProcessor<T>
{
    public EndpointDataObjectRequestProcessor(ObjectPersister<T> objectPersister, DataObjectValidator<T, DataObjectRequest<T>> validator)
    {
        super(new EndpointDataObjectPersister<>(objectPersister), validator);
    }

    public EndpointDataObjectRequestProcessor(ObjectPersister<T> objectPersister)
    {
        super(new EndpointDataObjectPersister<>(objectPersister));
    }
}
