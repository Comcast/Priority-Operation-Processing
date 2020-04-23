package com.comcast.pop.endpoint.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.AbstractStream;
import com.comcast.pop.api.TransformRequest;
import com.comcast.pop.api.input.InputFileResource;
import com.comcast.pop.api.input.InputStream;
import com.comcast.pop.api.output.OutputStream;
import com.comcast.pop.endpoint.base.validation.DataObjectValidator;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Transform Validator
 *
 * Checks fields for glaring issues (customerId, references)
 */
public class TransformValidator extends DataObjectValidator<TransformRequest, DataObjectRequest<TransformRequest>>
{
    private JsonHelper jsonHelper = new JsonHelper();
    private List<String> validationIssues;

    @Override
    public void validatePOST(DataObjectRequest<TransformRequest> request)
    {
        super.validatePOST(request);
        validationIssues = new LinkedList<>();

        TransformRequest transform = request.getDataObject();

        JsonNode rootTransformNode = jsonHelper.getObjectMapper().valueToTree(transform);

        validateInputs(transform.getInputs());
        validateReferences(transform, rootTransformNode);

        processValidationIssues(validationIssues);
    }

    protected void validateInputs(List<InputFileResource> inputFiles)
    {
        if(inputFiles == null || inputFiles.size() == 0)
        {
            validationIssues.add("Inputs are required. Please specify the input files.");
            return;
        }
        if(inputFiles.stream().anyMatch(input -> StringUtils.isBlank(input.getUrl())))
        {
            validationIssues.add("All inputs must specify a valid url.");
        }
    }

    protected void validateReferences(TransformRequest transformRequest, JsonNode rootTransformNode)
    {
        if(transformRequest.getInputStreams() != null)
        {
            validateInputStreams(transformRequest.getInputStreams().getAudio(), rootTransformNode);
            validateInputStreams(transformRequest.getInputStreams().getImage(), rootTransformNode);
            validateInputStreams(transformRequest.getInputStreams().getText(), rootTransformNode);
            validateInputStreams(transformRequest.getInputStreams().getVideo(), rootTransformNode);
        }
        if(transformRequest.getOutputStreams() != null)
        {
            validateOutputStreams(transformRequest.getOutputStreams().getAudio(), rootTransformNode);
            // TODO: will this be an output stream format?
            //validateOutputStreams(transformRequest.getOutputStreams().getImage(), rootTransformNode);
            validateOutputStreams(transformRequest.getOutputStreams().getText(), rootTransformNode);
            validateOutputStreams(transformRequest.getOutputStreams().getVideo(), rootTransformNode);
        }

        if(transformRequest.getOutputs() != null)
        {
            transformRequest.getOutputs().forEach(o ->
            {
                if (o.getOutputStreamRefs() != null && o.getOutputStreamRefs().size() > 0)
                {
                    o.getOutputStreamRefs().forEach(osr ->
                        validateReference(osr, o.getLabel(), rootTransformNode)
                    );
                }
                else
                {
                    validationIssues.add(String.format("Transform Request must include a value for required outputStreamRefs. Found issue on output - URL: %1$s, Type: %2$s",
                        o.getUrl(), o.getType()));
                }
            });
        }
    }

    protected void validateInputStreams(List<InputStream> inputStreams, JsonNode rootTransformNode)
    {
        if(inputStreams != null)
            inputStreams.forEach(is -> validateAbstractStream(is, rootTransformNode));
    }

    protected void validateOutputStreams(List<OutputStream> outputStreams, JsonNode rootTransformNode)
    {
        if(outputStreams != null)
            outputStreams.forEach(is -> validateAbstractStream(is, rootTransformNode));
    }

    protected void validateAbstractStream(AbstractStream dataStream, JsonNode rootTransformNode)
    {
        String reference = dataStream.getReference();
        validateReference(reference, dataStream.getType(), rootTransformNode);
    }

    protected void validateReference(String reference, String identifier, JsonNode rootTransformNode)
    {
        // TODO: when is a null reference acceptable?
        if(reference == null) return;

        if(StringUtils.isBlank(reference))
        {
            validationIssues.add(String.format("Null/empty stream reference found. Type: %1$s", identifier));
        }
        else
        {
            Object object = jsonHelper.getObjectFromRef(rootTransformNode, reference, Object.class);
            if (object == null)
            {
                validationIssues.add(String.format("Invalid stream reference found: %1$s", reference));
            }
        }
    }

    protected List<String> getValidationIssues()
    {
        return validationIssues;
    }

    protected void setValidationIssues(List<String> validationIssues)
    {
        this.validationIssues = validationIssues;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
