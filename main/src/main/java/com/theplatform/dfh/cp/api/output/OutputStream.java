package com.theplatform.dfh.cp.api.output;

import com.theplatform.dfh.cp.api.AbstractStream;

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
