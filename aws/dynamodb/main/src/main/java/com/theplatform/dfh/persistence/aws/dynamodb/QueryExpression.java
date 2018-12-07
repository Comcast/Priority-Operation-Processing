package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.util.StringUtils;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QueryExpression<T>
{
    protected static Logger logger = LoggerFactory.getLogger(QueryExpression.class);
    private static final String KEY_CONDITION = "%s = %s";
    private static final String QUERY_VALUE = ":%s";
    private TableIndexes tableIndexes;

    public QueryExpression(TableIndexes tableIndexes)
    {
        this.tableIndexes = tableIndexes;
    }

    public DynamoDBQueryExpression<T> forQuery(List<Query> queries)
    {
        if(queries == null || queries.size() == 0) return null;

        List<String> keyConditions = new ArrayList<>();
        Map<String, AttributeValue> awsQueryValueMap = new HashMap<String, AttributeValue>();
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();
        List<String> fields = new ArrayList<>();
        for(Query query : queries)
        {
            if (LimitField.fieldName().equals(query.getField().name()))
            {
                queryExpression.withLimit(query.getIntValue());
            }
            else
            {
                addCondition(keyConditions, awsQueryValueMap, query);
                fields.add(query.getField().name());
            }
        }
        if(tableIndexes != null)
            queryExpression.withIndexName(tableIndexes.getIndex(fields));

        queryExpression.withKeyConditionExpression(StringUtils.join(" AND ", keyConditions.toArray(new String[keyConditions.size()])))
            .withExpressionAttributeValues(awsQueryValueMap)
            .withConsistentRead(false);
        logger.info("DynamoDB query with key condition {} and value map {}", queryExpression.getKeyConditionExpression(), awsQueryValueMap.toString());

        return queryExpression;
    }
    public DynamoDBScanExpression forScan(List<Query> queries)
    {
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        for(Query query : queries)
        {
            if (!query.isCollection())
            {
                final String queryFieldName = query.getField().name();
                if (LimitField.fieldName().equals(queryFieldName))
                {
                    expression.withLimit(query.getIntValue());
                }
                else
                {
                    logger.info("DynamoDB scan with {} == {}", queryFieldName, query.getValue());
                    Condition condition =
                        new Condition()
                            .withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue().withS(query.getValue().toString()));
                    expression.addFilterCondition(query.getField().name(), condition);
                }
            }
        }
        return expression;
    }
    private void addCondition(List<String> conditions, Map<String, AttributeValue> valueMap, Query query)
    {
        final String awsQueryValueKey = String.format(QUERY_VALUE, query.getField().name());
        valueMap.put(awsQueryValueKey, new AttributeValue().withS(query.getValue().toString()));
        conditions.add(String.format(KEY_CONDITION, query.getField().name(), awsQueryValueKey));
    }
}
