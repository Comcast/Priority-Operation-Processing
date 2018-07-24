package com.theplatform.dfh.cp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.input.InputStreams;
import com.theplatform.dfh.cp.api.output.OutputFileResource;
import com.theplatform.dfh.cp.api.output.OutputStream;
import com.theplatform.dfh.cp.api.output.OutputStreams;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.api.input.InputStream;

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
     * Inputs: This includes URLs, creds,
     * and key metadata for the media files that DFH will process (VIDEO, AUDIO, TEXT tracks, etc.)
     * and metadata that serves as an input to DFH (e.g. chapter info for Ad Conditioning)
     */
    private List<InputFileResource> inputs;
    private InputStreams inputStreams;
    private OutputStreams outputStreams;
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
    public List<InputFileResource> getInputs()
    {
        return inputs;
    }

    @JsonProperty
    public void setInputs(List<InputFileResource> inputs)
    {
        this.inputs = inputs;
        fileReferencesByType = new FileResourceByTypeMap(inputs);
    }

    @JsonProperty
    public InputStreams getInputStreams()
    {
        return inputStreams;
    }

    @JsonProperty
    public void setInputStreams(InputStreams inputStreams)
    {
        this.inputStreams = inputStreams;
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
    public List<FileResource> getResourcesByType(String type)
    {
        return (fileReferencesByType != null) ? fileReferencesByType.getResourceByType(type) : null;
    }

    public void setParam(String name, Object value)
    {
        params.put(name, value);
    }
}
