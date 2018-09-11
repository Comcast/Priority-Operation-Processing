package com.theplatform.dfh.cp.api.input;

import com.theplatform.dfh.cp.api.AbstractStream;

public class InputStream extends AbstractStream
{
    public String getInputRef()
    {
        return getReference();
    }

    public void setInputRef(String sourceReference)
    {
        setReference(sourceReference);
    }
}
