package com.theplatform.dfh.cp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.api.input.InputStreams;
import com.theplatform.dfh.cp.api.output.OutputFileResource;
import com.theplatform.dfh.cp.api.output.OutputStreams;
import com.theplatform.dfh.cp.api.params.ParamsMap;

import java.util.List;

public class TransformRequest extends DefaultEndpointDataObject
{
    /**
     * Inputs: This includes URLs, creds,
     * and key metadata for the media files that will be processed (VIDEO, AUDIO, TEXT tracks, etc.)
     * and metadata that serves as an input(e.g. chapter info for Ad Conditioning)
     */
    private List<InputFileResource> inputs;
    private InputStreams inputStreams;
    private OutputStreams outputStreams;
    private List<OutputFileResource> outputs;

    /**
     * field to link Transforms, Agendas, and AgendaProgress
     */
    private String linkId;

    /**
     * Agenda template to use for mapping the transform request.
     */
    private String agendaTemplateTitle;
    private String agendaTemplateId;

    /**
     * Name value pairs for things like 'externalId'
     */
    private ParamsMap params = new ParamsMap();
    private FileResourceByTypeMap fileReferencesByType;

    public String getExternalId()
    {
        return params.getString("externalId");
    }
    public void setExternalId(String externalId)
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

    public String getLinkId()
    {
        return linkId;
    }

    public void setLinkId(String linkId)
    {
        this.linkId = linkId;
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

    public String getAgendaTemplateTitle()
    {
        return agendaTemplateTitle;
    }

    public void setAgendaTemplateTitle(String agendaTemplateTitle)
    {
        this.agendaTemplateTitle = agendaTemplateTitle;
    }

    public String getAgendaTemplateId()
    {
        return agendaTemplateId;
    }

    public void setAgendaTemplateId(String agendaTemplateId)
    {
        this.agendaTemplateId = agendaTemplateId;
    }
}
