package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.AbstractStream;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.input.InputStream;
import com.theplatform.dfh.cp.api.output.OutputStream;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Transform Validator
 *
 * Checks fields for glaring issues (customerId, references)
 */
public class TransformValidator
{
    private JsonHelper jsonHelper = new JsonHelper();
    private List<String> validationIssues;
    private final int MAX_ISSUES = 10;

    public void validate(TransformRequest transform)
    {
        validationIssues = new LinkedList<>();

        if(StringUtils.isBlank(transform.getCustomerId()))
            throw new ValidationException("The customer id must be specified on the transform.");

        JsonNode rootTransformNode = jsonHelper.getObjectMapper().valueToTree(transform);

        validateReferences(transform, rootTransformNode);

        if(validationIssues.size() > 0)
        {
            int lastIssueIndex = Math.min(validationIssues.size(), MAX_ISSUES);
            throw new ValidationException(String.format("Issues detected: %1$s%2$s",
                String.join(",", validationIssues.subList(0, lastIssueIndex)),
                lastIssueIndex < validationIssues.size() ? "[Truncating additional issues]" : "")
            );
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
                o.getOutputStreamRefs().forEach(osr ->
                    validateReference(osr, o.getLabel(), rootTransformNode)
                );
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
