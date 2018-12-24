package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.persistence.api.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaObjectRequest<T extends IdentifiedObject> extends LambdaRequest
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String BY_QUERY_PREFIX = "by";
    private Class<T> dataObjectClass;
    private List<Query> queries;

    public LambdaObjectRequest(JsonNode rootNode, Class<T> dataObjectClass)
    {
        super(rootNode);
        this.dataObjectClass = dataObjectClass;
    }

    public List<Query> getQueries()
    {
        return queries;
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

            return getObjectMapper().readValue(bodyText, dataObjectClass);
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
            return getURLDecodedValue(dataObjectId);

        //get the Id off the request parameters
        dataObjectId = (String) getRequestParamMap().get("id");
        if (dataObjectId != null)
            return getURLDecodedValue(dataObjectId);

        T dataObject = getDataObject();
        if(dataObject == null) return null;
        return dataObject.getId();
    }

    @Override
    protected void loadRequestParameters()
    {
        super.loadRequestParameters();
        if(getRequestParamMap() == null) return;

        // create all the by queries (if a param has the right prefix)
        queries = getRequestParamMap().entrySet().stream()
            .filter(e -> e.getKey().startsWith(BY_QUERY_PREFIX))
            .map(e -> new Query<>(e.getKey().substring(BY_QUERY_PREFIX.length()), getURLDecodedValue(e.getValue().toString())))
            .collect(Collectors.toList());
    }

    protected String getURLDecodedValue(String value)
    {
        try
        {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        }
        catch(UnsupportedEncodingException ex)
        {
            logger.error(String.format("%1$s encoding is not supported. The world is ending.", StandardCharsets.UTF_8.name()), ex);
        }
        return value;
    }
}
