package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.util.StringUtils;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QueryExpression<T>
{
    protected static Logger logger = LoggerFactory.getLogger(QueryExpression.class);
    private static final String KEY_CONDITION = "%s = %s";
    private static final String QUERY_VALUE = ":%s";
    private static final Integer LIMIT = 100;

    public DynamoDBQueryExpression<T> from(List<Query> queries)
    {
         List<String> keyConditions = new ArrayList<>();
        Map<String, AttributeValue> awsQueryValueMap = new HashMap<String, AttributeValue>();
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();
        if(queries == null || queries.size() == 0)
        {
            //getAll with limit
            queryExpression.withLimit(LIMIT)
                    .withConsistentRead(false);
        }
        else
        {
            final String indexName = queries.get(0).getField().name().toUpperCase() +"_INDEX";
            for(Query query : queries)
                addCondition(keyConditions, awsQueryValueMap, query);
            queryExpression.withKeyConditionExpression(StringUtils.join(" AND ", keyConditions.toArray(new String[keyConditions.size()])))
                    .withExpressionAttributeValues(awsQueryValueMap)
                    .withIndexName(indexName)
                    .withConsistentRead(false);
        }

        logger.info("DynamoDB key condition {}", queryExpression.getKeyConditionExpression());
        logger.info("DynamoDB value map {}", awsQueryValueMap.toString());
        return queryExpression;
    }
    private void addCondition(List<String> conditions, Map<String, AttributeValue> valueMap, Query query)
    {
        final String awsQueryValueKey = String.format(QUERY_VALUE, query.getField().name());
        valueMap.put(awsQueryValueKey, new AttributeValue().withS(query.getValue().toString()));
        conditions.add(String.format(KEY_CONDITION, query.getField().name(), awsQueryValueKey));
    }
}
