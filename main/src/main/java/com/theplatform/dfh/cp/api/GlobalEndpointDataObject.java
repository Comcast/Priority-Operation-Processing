package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.object.api.IdentifiedObject;

public interface GlobalEndpointDataObject extends IdentifiedObject
{
    public boolean isGlobal();
    public boolean getIsGlobal();
    public void setIsGlobal(boolean global);
}
