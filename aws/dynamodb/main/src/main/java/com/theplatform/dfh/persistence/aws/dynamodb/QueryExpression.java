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
    private Query primaryOrIndexQuery;
    private Query limitQuery;
    private List<Query> filterQueries = new ArrayList<>();

    public QueryExpression(TableIndexes tableIndexes, List<Query> queries)
    {
        this.tableIndexes = tableIndexes == null ? new TableIndexes() : tableIndexes;
        analyzeQueryTypes(queries);
    }

    public DynamoDBQueryExpression<T> forQuery()
    {
        if(primaryOrIndexQuery == null) return null;

        List<String> keyConditions = new ArrayList<>();
        List<String> filterConditions = new ArrayList<>();
        Map<String, AttributeValue> awsQueryValueMap = new HashMap<>();
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();

        //The first query that has an index is the index we use, the rest are filters off the data coming back.
        if (limitQuery != null)
        {
            queryExpression.withLimit(limitQuery.getIntValue());
        }

        addCondition(keyConditions, awsQueryValueMap, primaryOrIndexQuery);

        String index = tableIndexes.getIndex(primaryOrIndexQuery.getField().name());
        if (index != null)
        {
            queryExpression.withIndexName(index);
        }
        if(filterQueries != null)
        {
            for(Query filterQuery : filterQueries)
                addCondition(filterConditions, awsQueryValueMap, filterQuery);
            queryExpression.withFilterExpression(StringUtils.join(" AND ", filterConditions.toArray(new String[filterConditions.size()])));
        }

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

    private void analyzeQueryTypes(List<Query> queries)
    {
        if(queries == null) return;
        
        boolean foundIndex = false;
        for(Query query : queries)
        {
            String queryFieldName = query.getField().name();
            //The first query that has an index is the index we use, the rest are filters off the data coming back.
            if (LimitField.fieldName().equals(queryFieldName))
            {
                limitQuery = query;
            }
            else
            {
                if (!foundIndex)
                {
                    if (tableIndexes.isPrimary(queryFieldName) || tableIndexes.getIndex(queryFieldName) != null)
                    {
                        foundIndex = true;
                        primaryOrIndexQuery = query;
                    }
                    else
                    {
                        filterQueries.add(query);
                    }
                }
                else
                {
                    filterQueries.add(query);
                }
            }
        }
    }

}
