package com.theplatform.dfh.cp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.output.OutputFileResource;
import com.theplatform.dfh.cp.api.output.OutputStream;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.source.SourceFileResource;
import com.theplatform.dfh.cp.api.source.SourceStream;

import java.net.URI;
import java.util.List;

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
     * and key metadata for the media files that DFH will process (VIDEO, AUDIO, TEXT tracks, etc.)
     * and metadata that serves as an input to DFH (e.g. chapter info for Ad Conditioning)
     */
    private List<SourceFileResource> sources;
    private List<SourceStream> sourceStreams;
    private List<OutputStream> outputStreams;
    private List<OutputFileResource> outputs;
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
    public List<SourceFileResource> getSources()
    {
        return sources;
    }

    @JsonProperty
    public void setSources(List<SourceFileResource> sources)
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
    public List<OutputFileResource> getOutputs()
    {
        return outputs;
    }

    @JsonProperty
    public void setOutputs(List<OutputFileResource> outputs)
    {
        this.outputs = outputs;
    }

    @JsonProperty
    public ParamsMap getParams()
    {
        return params;
    }

    @JsonProperty
    public void setParams(ParamsMap params)
    {
        this.params = params;
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
