package com.theplatform.dfh.cp.api.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.FileResource;
import com.theplatform.dfh.cp.api.TargetResource;

import java.util.List;

public class OutputResource extends TargetResource
{
    /**
     * Referencing streams to use.
     */
    private List<String> outputStreamRefs;


    /**
     * Protection key information
     */
    private String protectionScheme;

    @JsonProperty
    public String getFormat()
    {
        return getParams().getString(OutputParams.format);
    }

    @JsonProperty
    public void setFormat(String format)
    {
        getParams().put(OutputParams.format, format);
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
        return protectionScheme;
    }

    @JsonProperty
    public void setProtectionScheme(String protectionScheme)
    {
        this.protectionScheme = protectionScheme;
    }

}
