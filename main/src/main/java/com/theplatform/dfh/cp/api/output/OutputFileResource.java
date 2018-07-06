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
    public String getFormat()
    {
        return getParams().getString(OutputParamKey.format);
    }

    @JsonProperty
    public void setFormat(String format)
    {
        getParams().put(OutputParamKey.format, format);
    }

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

    @JsonProperty
    public String getProtectionScheme()
    {
        return getParams().getString(OutputParamKey.protectionScheme);
    }

    @JsonProperty
    public void setProtectionScheme(String protectionScheme)
    {
        getParams().put(OutputParamKey.protectionScheme, protectionScheme);
    }

}
