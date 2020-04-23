package com.comcast.pop.api.input;

import com.comcast.pop.api.AbstractStream;

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
