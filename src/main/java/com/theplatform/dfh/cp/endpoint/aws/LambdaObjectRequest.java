package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LambdaObjectRequest<T extends IdentifiedObject> extends LambdaRequest
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Class<T> dataObjectClass;

    public LambdaObjectRequest(JsonNode rootNode, Class<T> dataObjectClass)
    {
        super(rootNode);
        this.dataObjectClass = dataObjectClass;
    }

    protected T getDataObject() throws BadRequestException
    {
        try
        {
            JsonNode bodyNode = getJsonNode().at(BODY_PATH);
            if(bodyNode.isMissingNode())
            {
                // TODO: further decide how this is handled...
                return null;
            }
            String bodyText = bodyNode.asText();
            if(StringUtils.isBlank(bodyText))
            {
                return null;
            }

            return objectMapper.readValue(StringEscapeUtils.unescapeJson(bodyText), dataObjectClass);
        }
        catch (IOException e)
        {
            throw new BadRequestException("Request body is not recognized as '" + dataObjectClass.getName() + "'", e);
        }
    }

    protected String getDataObjectId() throws BadRequestException
    {
        //first see if it's on the path parameter.
        String dataObjectId = getIdFromPathParameter();
        if (dataObjectId != null)
            return dataObjectId;

        //get the Id off the request parameters
        dataObjectId = (String) getRequestParamMap().get("id");
        if (dataObjectId != null)
            return dataObjectId;

        T dataObject = getDataObject();
        if(dataObject == null) return null;
        return dataObject.getId();
    }
}
