package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.theplatform.dfh.persistence.api.field.CountField;
import com.theplatform.dfh.persistence.api.field.FieldsField;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates the necessary DynamoDB Query and Scan expressions from our ByQuery objects.
 * DynamoDB requires you have a primary or index (partition + optional sort) key for queries. This class requires you specify
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
 * If linkId (partition key) and customerId (sort key) is associated to an index we do the same type of query but with the index specified.
 * byLinkId(), byCustomerId() byTitle()
 * keyExpression : linkId and customerId, filterExpression : title
 * indexName = link_index (assuming the table index has the partition and sort key specified)
 *
 * Scan is also supported and should only be used in cases where a full table scan is necessary, ie: getAll()
 * @param <T>
 */
public class QueryExpression<T>
{
    protected static Logger logger = LoggerFactory.getLogger(QueryExpression.class);
    private static final String KEY_CONDITION = "%s = %s";
    private static final String QUERY_VALUE = ":%s";
    private static final String AND_STATEMENT = " AND ";
    private TableIndexes tableIndexes;
    private TableIndex queryIndex;
    private List<Query> primaryKeyQueries;
    private Query limitQuery;
    private Select selectQuery;
    private List<Query> filterQueries = new ArrayList<>();
    private String filterAttributes;

    public QueryExpression(TableIndexes tableIndexes, List<Query> queries)
    {
        this.tableIndexes = tableIndexes == null ? new TableIndexes() : tableIndexes;
        primaryKeyQueries = new LinkedList<>();
        analyzeQueryTypes(queries);
    }

    public DynamoDBQueryExpression<T> forQuery()
    {
        if(primaryKeyQueries.size() == 0)
        {
            logger.warn("No queries found with primary key field.");
            return null;
        }

        Map<String, AttributeValue> awsQueryValueMap = new HashMap<>();
        DynamoDBQueryExpression<T> expression = new DynamoDBQueryExpression<>();

        // add in the primary key queries (may optionally include the sort key)
        expression.withKeyConditionExpression(String.join(AND_STATEMENT, generateConditions(primaryKeyQueries, awsQueryValueMap)))
            .withExpressionAttributeValues(awsQueryValueMap)
            .withConsistentRead(false);

        if(filterAttributes != null)
        {
            expression.withProjectionExpression(filterAttributes);
        }
        if(queryIndex != null)
        {
            expression.withIndexName(queryIndex.getName());
        }
        if(hasQueries())
        {
            expression.withFilterExpression(String.join(AND_STATEMENT, generateConditions(filterQueries, awsQueryValueMap)));
        }
        if (limitQuery != null)
        {
            expression.withLimit(limitQuery.getIntValue());
        }
        if(selectQuery != null)
        {
            expression.withSelect(selectQuery);
        }

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
            Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(query.getValue().toString()));
            expression.addFilterCondition(query.getField().name(), condition);
        }
        if(filterAttributes != null)
        {
            expression.withProjectionExpression(filterAttributes);
        }
        logger.info("DynamoDB scan with {}", filterQueries.stream()
            .map(query -> query.getField().name() + " == " + query.getValue())
            .collect(Collectors.joining(",")));
        return expression;
    }

    public boolean hasKey()
    {
        return primaryKeyQueries.size() > 0;
    }
    public boolean hasQueries()
    {
        return filterQueries != null && filterQueries.size() > 0 || hasKey();
    }
    public boolean hasCount()
    {
        return this.selectQuery != null && selectQuery == Select.COUNT;
    }
    private List<String> generateConditions(List<Query> queries, Map<String, AttributeValue> valueMap)
    {
        List<String> conditions = new LinkedList<>();
        queries.forEach(query ->
        {
            final String awsQueryValueKey = String.format(QUERY_VALUE, query.getField().name());
            valueMap.put(awsQueryValueKey, new AttributeValue().withS(query.getValue().toString()));
            conditions.add(String.format(KEY_CONDITION, query.getField().name(), awsQueryValueKey));
        });
        return conditions;
    }

    /**
     * The Query objects are separated by primary/index, limit, or filter.
     * @param queries byQueries to query upon
     */
    private void analyzeQueryTypes(List<Query> queries)
    {
        if(queries == null || queries.size() == 0) return;

        // look for the best fit index based on all the fields being queried
        queryIndex = tableIndexes.getBestTableIndexMatch(queries.stream().map(q -> q.getField().name()).collect(Collectors.toList()));

        for(Query query : queries)
        {
            String queryFieldName = query.getField().name();
            //The first query that has an index is the index we use, the rest are filters off the data coming back.
            if (LimitField.fieldName().equals(queryFieldName))
            {
                limitQuery = query;
            }
            else if(FieldsField.fieldName().equals(queryFieldName))
            {
                filterAttributes = query.getStringValue();
            }
            else if(CountField.fieldName().equals(queryFieldName))
            {
                selectQuery = Select.COUNT;
            }
            else
            {
                if(queryIndex == null)
                {
                    if(tableIndexes.isPrimary(queryFieldName))
                    {
                        primaryKeyQueries.add(query);
                    }
                    else
                    {
                        filterQueries.add(query);
                    }
                }
                else
                {
                    if(queryIndex.isPrimaryKey(queryFieldName))
                    {
                        primaryKeyQueries.add(query);
                    }
                    else
                    {
                        filterQueries.add(query);
                    }
                }
            }
        }
    }

}
