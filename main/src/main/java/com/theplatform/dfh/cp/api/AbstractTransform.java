package com.theplatform.dfh.cp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.output.OutputResource;
import com.theplatform.dfh.cp.api.output.OutputStream;
import com.theplatform.dfh.cp.api.source.SourceStream;
import com.theplatform.dfh.cp.api.source.TextResource;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTransform
{
    private URI jobId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public URI getId()
    {
        return jobId;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public void setId(URI jobId)
    {
        this.jobId = jobId;
    }
    /**
     * Sources: This includes URLs, creds,
     * and key metadata for the media files that DFH will process (video, audio, text tracks, etc.)
     * and metadata that serves as an input to DFH (e.g. chapter info for Ad Conditioning)
     */
    private List<TargetResource> sources;
    private List<SourceStream> sourceStreams;
    private List<OutputStream> outputStreams;
    private List<OutputResource> outputs;
    /**
     * Name value pairs for things like 'externalId'
     */
    private ParamsMap params = new ParamsMap();
    private FileResourceByTypeMap fileReferencesByType;

    public URI getExternalId()
    {
        return (URI)params.get("externalId");
    }
    public void setExternalId(URI externalId)
    {
        params.put("externalId", externalId);
    }

    @JsonProperty
    public List<TargetResource> getSources()
    {
        return sources;
    }

    @JsonProperty
    public void setSources(List<TargetResource> sources)
    {
        this.sources = sources;
        fileReferencesByType = new FileResourceByTypeMap(sources);
    }

    @JsonProperty
    public List<SourceStream> getSourceStreams()
    {
        return sourceStreams;
    }

    @JsonProperty
    public void setSourceStreams(List<SourceStream> sourceStreams)
    {
        this.sourceStreams = sourceStreams;
    }

    @JsonProperty
    public List<OutputStream> getOutputStreams()
    {
        return outputStreams;
    }

    @JsonProperty
    public void setOutputStreams(List<OutputStream> outputStreams)
    {
        this.outputStreams = outputStreams;
    }

    @JsonProperty
    public List<OutputResource> getOutputs()
    {
        return outputs;
    }

    @JsonProperty
    public void setOutputs(List<OutputResource> outputs)
    {
        this.outputs = outputs;
    }

    @JsonProperty
    public ParamsMap getParams()
    {
        return params;
    }


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public List<FileResource> getVideoSources()
    {
        return (fileReferencesByType != null) ? fileReferencesByType.getVideoResources() : null;
    }
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public List<FileResource> getTextSources()
    {
        return (fileReferencesByType != null) ? fileReferencesByType.getTextResources() : null;
    }


    public void setParam(String name, Object value)
    {
        params.put(name, value);
    }
}
