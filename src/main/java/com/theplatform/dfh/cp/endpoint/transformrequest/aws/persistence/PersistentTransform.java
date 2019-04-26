package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.theplatform.dfh.cp.api.FileResource;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.api.input.InputStreams;
import com.theplatform.dfh.cp.api.output.OutputFileResource;
import com.theplatform.dfh.cp.api.output.OutputStreams;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.endpoint.persistence.ParamsMapConverter;

import java.util.Date;
import java.util.List;

/**
 */
public class PersistentTransform extends TransformRequest
{

    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }

    @Override
    public Date getUpdatedTime()
    {
        return super.getUpdatedTime();
    }

    @Override
    public Date getAddedTime()
    {
        return super.getAddedTime();
    }

    @Override
    @DynamoDBTypeConverted(converter = ParamsMapConverter.class)
    @DynamoDBAttribute(attributeName = "params")
    public ParamsMap getParams()
    {
        return super.getParams();
    }

    @Override
    @DynamoDBIgnore
    public String getExternalId()
    {
        return super.getExternalId();
    }

    @Override
    @DynamoDBTypeConverted(converter = ListInputFileResourceConverter.class)
    @DynamoDBAttribute(attributeName = "inputs")
    public List<InputFileResource> getInputs()
    {
        return super.getInputs();
    }

    @Override
    @DynamoDBTypeConverted(converter = InputStreamsConverter.class)
    @DynamoDBAttribute(attributeName = "inputStreams")
    public InputStreams getInputStreams()
    {
        return super.getInputStreams();
    }

    @Override
    @DynamoDBTypeConverted(converter = OutputStreamsConverter.class)
    @DynamoDBAttribute(attributeName = "outputStreams")
    public OutputStreams getOutputStreams()
    {
        return super.getOutputStreams();
    }

    @Override
    @DynamoDBTypeConverted(converter = ListOutputFileResourceConverter.class)
    @DynamoDBAttribute(attributeName = "outputs")
    public List<OutputFileResource> getOutputs()
    {
        return super.getOutputs();
    }

    @Override
    @DynamoDBIgnore
    public List<FileResource> getResourcesByType(String type)
    {
        return super.getResourcesByType(type);
    }

    @Override
    public String getLinkId()
    {
        return super.getLinkId();
    }

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
    }

    @Override
    public String getCid()
    {
        return super.getCid();
    }
}
