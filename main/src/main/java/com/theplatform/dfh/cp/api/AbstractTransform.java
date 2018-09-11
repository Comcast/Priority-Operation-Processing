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
    private URI id;

    public URI getId()
    {
        return id;
    }

    public void setId(URI id)
    {
        this.id = id;
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

    public List<InputFileResource> getInputs()
    {
        return inputs;
    }

    public void setInputs(List<InputFileResource> inputs)
    {
        this.inputs = inputs;
        fileReferencesByType = new FileResourceByTypeMap(inputs);
    }

    public InputStreams getInputStreams()
    {
        return inputStreams;
    }

    public void setInputStreams(InputStreams inputStreams)
    {
        this.inputStreams = inputStreams;
    }

    public OutputStreams getOutputStreams()
    {
        return outputStreams;
    }

    public void setOutputStreams(OutputStreams outputStreams)
    {
        this.outputStreams = outputStreams;
    }

    public List<OutputFileResource> getOutputs()
    {
        return outputs;
    }

    public void setOutputs(List<OutputFileResource> outputs)
    {
        this.outputs = outputs;
    }

    public ParamsMap getParams()
    {
        return params;
    }

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
