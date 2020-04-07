package com.comcast.pop.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.object.api.IdentifiedObject;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaDataObjectRequest<T extends IdentifiedObject> extends LambdaRequest<T> implements DataObjectRequest<T>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String BY_QUERY_PREFIX = "by";
    private DefaultDataObjectRequest<T> dataObjectRequest = new DefaultDataObjectRequest<>();

    public LambdaDataObjectRequest(JsonNode rootNode, Class<T> dataObjectClass)
    {
        super(rootNode, dataObjectClass);

        if(rootNode == null) return;
        T dataObject = getPayload();
        dataObjectRequest.setDataObject(dataObject);

        String id = parseId();
        dataObjectRequest.setId(id);

        parseQueries();
    }

    public List<Query> getQueries()
    {
        return dataObjectRequest.getQueries();
    }

    @Override
    public String getId()
    {
        return dataObjectRequest.getId();
    }

    @Override
    public T getDataObject()
    {
        return dataObjectRequest.getDataObject();
    }

    public String parseId() throws BadRequestException
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

    private void parseQueries()
    {
        if(getRequestParamMap() == null) return;

        // create all the by queries (if a param has the right prefix)
        List<Query> queries = getRequestParamMap().entrySet().stream()
            .filter(e -> e.getKey().startsWith(BY_QUERY_PREFIX))
            .map(e -> new Query<>(e.getKey().substring(BY_QUERY_PREFIX.length()), getURLDecodedValue(e.getValue().toString())))
            .collect(Collectors.toList());
        dataObjectRequest.setQueries(queries);
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

    @Override
    public void setQueries(List<Query> queries)
    {
        dataObjectRequest.setQueries(queries);
    }
}
