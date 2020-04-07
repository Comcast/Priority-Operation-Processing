package com.theplatform.dfh.persistence.aws.dynamodb.retrieve;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.comcast.pop.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.aws.dynamodb.QueryExpression;

import java.util.List;

public abstract class ObjectRetriever<T extends IdentifiedObject>
{
    protected final QueryExpression<T> queryExpression;
    protected final Class<T> dataObjectClass;
    protected final DynamoDBMapper dynamoDBMapper;

    public ObjectRetriever()
    {
        this(null, null, null);
    }

    public ObjectRetriever(QueryExpression<T> queryExpression, Class<T> dataObjectClass, DynamoDBMapper dynamoDBMapper)
    {
        this.queryExpression = queryExpression;
        this.dataObjectClass = dataObjectClass;
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Retrieves the objects based on the query specified in the constructor.
     * @return List of zero or more objects based on the query
     */
    public abstract List<T> retrieveObjects();
}
