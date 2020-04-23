package com.comcast.pop.api;

import com.comcast.pop.object.api.IdentifiedObject;

public interface GlobalEndpointDataObject extends IdentifiedObject
{
    Boolean isGlobal();
    Boolean getIsGlobal();
    void setIsGlobal(Boolean global);
}
