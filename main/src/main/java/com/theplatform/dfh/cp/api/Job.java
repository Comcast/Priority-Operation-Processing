package com.theplatform.dfh.cp.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.output.Output;
import com.theplatform.dfh.cp.api.output.OutputStreams;
import com.theplatform.dfh.cp.api.source.SourceStreams;
import com.theplatform.dfh.cp.api.source.Sources;

import java.util.List;

public class Job
{
    Sources sources;
    SourceStreams sourceStreams;
    OutputStreams outputStreams;
    List<Output> outputs;

    public Job()
    {
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
}