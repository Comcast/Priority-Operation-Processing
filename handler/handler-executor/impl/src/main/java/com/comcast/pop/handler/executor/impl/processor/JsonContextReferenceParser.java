package com.comcast.pop.handler.executor.impl.processor;

import java.util.Set;

public interface JsonContextReferenceParser
{
    /**
     * Extracts the operation names from the references specified
     * @param references The references to re-map to operation names
     * @return Set of operation names.
     */
    Set<String> getOperationNames(Set<String> references);
}
