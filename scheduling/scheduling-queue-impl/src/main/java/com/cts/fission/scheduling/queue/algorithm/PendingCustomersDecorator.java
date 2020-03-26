package com.cts.fission.scheduling.queue.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator for customer ids
 */
public class PendingCustomersDecorator
{
    private final List<String> pendingCustomerIds;
    private final boolean isFullSet;

    public PendingCustomersDecorator(List<String> pendingCustomerIds, boolean isFullSet)
    {
        this.pendingCustomerIds = pendingCustomerIds;
        this.isFullSet = isFullSet;
    }

    /**
     * Gets a copy of the pending customerIds
     * @return The pending customer ids
     */
    public List<String> getPendingCustomerIds()
    {
        if(pendingCustomerIds == null) return new ArrayList<>();
        return new ArrayList<>(pendingCustomerIds);
    }

    public boolean isFullSet()
    {
        return isFullSet;
    }
}
