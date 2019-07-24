package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.amazonaws.util.StringUtils;
import com.theplatform.dfh.persistence.api.field.CountField;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Creates the necessary DynamoDB Query and Scan expressions from our ByQuery objects.
 * DynamoDB requires you have a primary or index key for queries. This class requires you specify
 * the table indexes.
 * Example:
 * The following would query by primary key 'id' then filter the results by title before returning the results.
 * byId() and byTitle()
 * keyExpression : id, filterExpression : title
 *
 * If linkId is associated to an index we do the same type of query but with the index specified.
 * byLinkId() and byTitle()
 * keyExpression : linkId, filterExpression : title
 * indexName = link_index
 *
 * Span is also supported and should only be used in cases where a full table scan is necessary, ie: getAll()
 * @param <T>
 */
public class QueryExpression<T>
{
    protected static Logger logger = LoggerFactory.getLogger(QueryExpression.class);
    private static final String KEY_CONDITION = "%s = %s";
    private static final String QUERY_VALUE = ":%s";
    private TableIndexes tableIndexes;
    private Query primaryOrIndexQuery;
    private Query limitQuery;
    private Select selectQuery;
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
        DynamoDBQueryExpression<T> expression = new DynamoDBQueryExpression<T>();

        //The first query that has an index is the index we use, the rest are filters off the data coming back.
        if (limitQuery != null)
        {
            expression.withLimit(limitQuery.getIntValue());
        }

        addCondition(keyConditions, awsQueryValueMap, primaryOrIndexQuery);

        String index = tableIndexes.getIndex(primaryOrIndexQuery.getField().name());
        if (index != null)
        {
            expression.withIndexName(index);
        }
        if(filterQueries != null && filterQueries.size() > 0)
        {
            for(Query filterQuery : filterQueries)
                addCondition(filterConditions, awsQueryValueMap, filterQuery);
            expression.withFilterExpression(StringUtils.join(" AND ", filterConditions.toArray(new String[filterConditions.size()])));
        }
        if(selectQuery != null)
            expression.withSelect(selectQuery);
        expression.withKeyConditionExpression(StringUtils.join(" AND ", keyConditions.toArray(new String[keyConditions.size()])))
            .withExpressionAttributeValues(awsQueryValueMap)
            .withConsistentRead(false);

        logger.info("DynamoDB query with key condition {} and value map {}", expression.getKeyConditionExpression(), awsQueryValueMap.toString());

        return expression;
    }

    /**
     * Where no primary key is available.
     * @return DynanoDBScanExpression The scan expression dynamodb needs for scanning
     */
    public DynamoDBScanExpression forScan()
    {
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        Integer limit = limitQuery != null ? limitQuery.getIntValue() : LimitField.defaultValue();
        expression.withLimit(limit);

        for(Query query : filterQueries)
        {
            String queryFieldName = query.getField().name();
            logger.info("DynamoDB scan with {} == {}", queryFieldName, query.getValue());
            Condition condition =
                    new Condition()
                            .withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue().withS(query.getValue().toString()));
            expression.addFilterCondition(query.getField().name(), condition);
        }
        return expression;
    }

    public boolean hasKey()
    {
        return this.primaryOrIndexQuery != null;
    }
    public boolean hasCount()
    {
        return this.selectQuery != null && selectQuery == Select.COUNT;
    }
    private void addCondition(List<String> conditions, Map<String, AttributeValue> valueMap, Query query)
    {
        final String awsQueryValueKey = String.format(QUERY_VALUE, query.getField().name());
        valueMap.put(awsQueryValueKey, new AttributeValue().withS(query.getValue().toString()));
        conditions.add(String.format(KEY_CONDITION, query.getField().name(), awsQueryValueKey));
    }

    /**
     * The Query objects are separated by primary/index, limit, or filter.
     * @param queries byQueries to query upon
     */
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
            else if(CountField.fieldName().equals(queryFieldName))
            {
                selectQuery = Select.COUNT;
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
