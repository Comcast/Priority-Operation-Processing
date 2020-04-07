package com.comcast.pop.api.output;

import com.comcast.pop.api.AbstractStream;

public class OutputStream extends AbstractStream
{
    public String getOutputRef()
    {
        return getReference();
    }

    public void setOutputRef(String sourceStreamReference)
    {
        setReference(sourceStreamReference);
    }

}
