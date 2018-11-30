package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.util.StringUtils;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryExpression<T>
{
    protected static Logger logger = LoggerFactory.getLogger(QueryExpression.class);
    private static final String KEY_CONDITION = "%s = %s";
    private static final String QUERY_VALUE = ":%s";

    public DynamoDBQueryExpression<T> from(List<Query> queries)
    {
        if(queries == null || queries.size() == 0) return null;

        List<String> keyConditions = new ArrayList<>();
        Map<String, AttributeValue> awsQueryValueMap = new HashMap<String, AttributeValue>();
        for(Query query : queries)
            addCondition(keyConditions, awsQueryValueMap, query);
        final String indexName = queries.get(0).getField().name().toUpperCase() +"_INDEX";
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
            .withKeyConditionExpression(StringUtils.join(" AND ", keyConditions.toArray(new String[keyConditions.size()])))
            .withExpressionAttributeValues(awsQueryValueMap)
            .withIndexName(indexName)
            .withConsistentRead(false);

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
