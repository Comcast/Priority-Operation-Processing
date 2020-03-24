package com.theplatform.dfh.object.api;

import java.util.UUID;

/**
 * ID Generator for Identified objects (UUID by default)
 */
public class UUIDGenerator implements IdGenerator
{
    public String generate()
    {
        return UUID.randomUUID().toString();
    }
}
