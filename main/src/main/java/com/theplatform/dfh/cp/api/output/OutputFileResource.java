package com.theplatform.dfh.cp.api.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.AbstractFileResource;

import java.util.List;

public class OutputFileResource extends AbstractFileResource
{
    /**
     * Referencing streams to use.
     */
    private List<String> outputStreamRefs;

    @JsonProperty
    public List<String> getOutputStreamRefs()
    {
        return outputStreamRefs;
    }

    @JsonProperty
    public void setOutputStreamRefs(List<String> outputStreamRefs)
    {
        this.outputStreamRefs = outputStreamRefs;
    }

}
