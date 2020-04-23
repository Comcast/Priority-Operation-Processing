package com.comcast.pop.endpoint.base;

import com.comcast.pop.api.DefaultEndpointDataObject;

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
    public static void applyAddedTime(DefaultEndpointDataObject endpointDataObject)
    {
        Date now = new Date();
        endpointDataObject.setAddedTime(now);
        endpointDataObject.setUpdatedTime(now);
    }

    /**
     * Updates the updated time to now
     * @param endpointDataObject The object to update
     */
    public static void applyUpdatedTime(DefaultEndpointDataObject endpointDataObject)
    {
        endpointDataObject.setUpdatedTime(new Date());
    }
}
