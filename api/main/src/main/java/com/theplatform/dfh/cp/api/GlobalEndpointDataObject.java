package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.object.api.IdentifiedObject;

public interface GlobalEndpointDataObject extends IdentifiedObject
{
    Boolean isGlobal();
    Boolean getIsGlobal();
    void setIsGlobal(Boolean global);
}
