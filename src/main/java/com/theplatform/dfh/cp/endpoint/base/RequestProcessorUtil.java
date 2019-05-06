package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.api.EndpointDataObject;

import java.util.Date;

/**
 * Central utilities for updating the added and update time.
 */
public class RequestProcessorUtil
{
    /**
     * Updates the added and updated time to now
     * @param endpointDataObject The object to update
     */
    public static void applyAddedTime(EndpointDataObject endpointDataObject)
    {
        Date now = new Date();
        endpointDataObject.setAddedTime(now);
        endpointDataObject.setUpdatedTime(now);
    }

    /**
     * Updates the updated time to now
     * @param endpointDataObject The object to update
     */
    public static void applyUpdatedTime(EndpointDataObject endpointDataObject)
    {
        endpointDataObject.setUpdatedTime(new Date());
    }
}
