package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 */
public class LambdaServiceRequest<T> extends LambdaRequest
{

    private Class<T> clazz;

    public LambdaServiceRequest(JsonNode rootNode, Class<T> clazz)
    {
        this(rootNode);
        this.clazz = clazz;
    }

    public LambdaServiceRequest(JsonNode rootNode)
    {
        super(rootNode);
    }

    public T getRequestObject() throws BadRequestException
    {
        try
        {
            JsonNode bodyNode = getJsonNode().at(BODY_PATH);
            if (bodyNode.isMissingNode())
            {
                // TODO: further decide how this is handled...
                return null;
            }
            String bodyText = bodyNode.asText();
            if (StringUtils.isBlank(bodyText))
            {
                return null;
            }

            return getObjectMapper().readValue(bodyText, clazz);
        }
        catch (IOException e)
        {
            throw new BadRequestException("Request body is not recognized as '" + clazz.getName() + "'", e);
        }
    }
}
