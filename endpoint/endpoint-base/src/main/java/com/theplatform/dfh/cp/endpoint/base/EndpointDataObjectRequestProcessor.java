package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.api.DefaultEndpointDataObject;
import com.theplatform.dfh.cp.endpoint.base.persistence.EndpointDataObjectPersister;
import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.comcast.fission.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * DefaultEndpointDataObject specific request processor. This wraps the incoming object persister.
 *
 * This is necessary because not all our objects are EndpointDataObjects with time fields.
 *
 * @param <T> an DefaultEndpointDataObject type
 */
public class EndpointDataObjectRequestProcessor<T extends DefaultEndpointDataObject> extends DataObjectRequestProcessor<T>
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
