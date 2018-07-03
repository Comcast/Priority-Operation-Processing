package com.theplatform.dfh.cp.api.output;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.AbstractStream;


public class OutputStream extends AbstractStream
{
    @JsonProperty
    public String getSourceStreamReference()
    {
        return getReference();
    }

    @JsonProperty
    public void setSourceStreamReference(String sourceStreamReference)
    {
        setReference(sourceStreamReference);
    }

}
