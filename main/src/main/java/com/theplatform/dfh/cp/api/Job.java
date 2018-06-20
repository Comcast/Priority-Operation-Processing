package com.theplatform.dfh.cp.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.output.Output;
import com.theplatform.dfh.cp.api.output.OutputStreams;
import com.theplatform.dfh.cp.api.source.SourceStreams;
import com.theplatform.dfh.cp.api.source.Sources;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class Job
{
    private URI jobId;
    private URI externalId;
    private Sources sources;
    private SourceStreams sourceStreams;
    private OutputStreams outputStreams;
    private List<Output> outputs;
    /**
     * Name value pairs for things like 'externalId'
     */
    private Map<String, String> metadata;

    public Job()
    {
    }

    @JsonProperty
    public URI getJobId()
    {
        return jobId;
    }

    @JsonProperty
    public void setJobId(URI jobId)
    {
        this.jobId = jobId;
    }

    @JsonProperty
    public URI getExternalId()
    {
        return externalId;
    }

    @JsonProperty
    public void setExternalId(URI externalId)
    {
        this.externalId = externalId;
    }

    @JsonProperty
    public Sources getSources()
    {
        return sources;
    }

    @JsonProperty
    public void setSources(Sources sources)
    {
        this.sources = sources;
    }

    @JsonProperty
    public SourceStreams getSourceStreams()
    {
        return sourceStreams;
    }

    @JsonProperty
    public void setSourceStreams(SourceStreams sourceStreams)
    {
        this.sourceStreams = sourceStreams;
    }

    @JsonProperty
    public OutputStreams getOutputStreams()
    {
        return outputStreams;
    }

    @JsonProperty
    public void setOutputStreams(OutputStreams outputStreams)
    {
        this.outputStreams = outputStreams;
    }

    @JsonProperty
    public List<Output> getOutputs()
    {
        return outputs;
    }

    @JsonProperty
    public void setOutputs(List<Output> outputs)
    {
        this.outputs = outputs;
    }

    @JsonProperty
    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    @JsonProperty
    public void setMetadata(Map<String, String> metadata)
    {
        this.metadata = metadata;
    }
}