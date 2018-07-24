package com.theplatform.dfh.cp.api.input;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.AbstractStream;

public class InputStream extends AbstractStream
{

    @JsonProperty
    public String getInputRef()
    {
        return getReference();
    }

    @JsonProperty
    public void setInputRef(String sourceReference)
    {
        setReference(sourceReference);
    }
}
