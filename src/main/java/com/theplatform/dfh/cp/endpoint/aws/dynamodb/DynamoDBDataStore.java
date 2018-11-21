package com.theplatform.dfh.cp.endpoint.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.DataObjectFeed;
import com.theplatform.dfh.cp.endpoint.api.persistence.DataStore;
import com.theplatform.dfh.cp.endpoint.api.persistence.DataStoreException;
import com.theplatform.dfh.cp.endpoint.api.query.Query;
import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class DynamoDBDataStore<D extends IdentifiedObject> implements DataStore<D>
{
    private static final Logger logger = LoggerFactory.getLogger(DynamoDBDataStore.class);
    private static final String KEY_CONDITION = "%s = :%s";
    private static final String QUERY_VALUE = ":%s";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Table table;
    private final Class dataObjectClass;

    public DynamoDBDataStore(String tableName, Class dataObjectClass)
    {
        this(new DynamoDB(AmazonDynamoDBClientBuilder.standard().build()), tableName, dataObjectClass);
    }
    public DynamoDBDataStore(DynamoDB dynamoDB, String tableName, Class dataObjectClass)
    {
        this.table = dynamoDB.getTable(tableName);
        this.dataObjectClass = dataObjectClass;
    }

    @Override
    public D retrieve(String id) throws DataStoreException
    {
         ItemCollection items = query(table, Collections.singleton(new Query()));
        DataObjectFeed<D> feed = getResponseFeed(items, new ArrayList<>());
        if(feed == null || feed.getAll() == null) return null;
        return feed.getAll().get(0);
    }

    @Override
    public DataObjectFeed<D> retrieve(List<Query> queries) throws DataStoreException
    {
        ItemCollection items = scan(table, queries);
        return getResponseFeed(items, queries);
    }

    @Override
    public void persist(String id, D dataObject) throws DataStoreException
    {
        try
        {
            Item item = new Item();
            BeanMap beanMap = new BeanMap(dataObject);
            Iterator<Map.Entry<Object, Object>> propertyIterator = beanMap.entryIterator();
            while(propertyIterator.hasNext())
            {
                Map.Entry<Object, Object> entry = propertyIterator.next();
                item.with(entry.getKey().toString(), entry.getValue());
            }
            table.putItem(item);
        }
        catch (Exception e)
        {
            throw new DataStoreException(String.format("Unable to persist data for id {}", id), e);
        }
    }

    @Override
    public void update(String id, D dataObject) throws DataStoreException
    {
//@todo
    }

    @Override
    public void delete(String id) throws DataStoreException
    {
        table.deleteItem(new PrimaryKey("id", id));
    }

    private DataObjectFeed<D> getResponseFeed(ItemCollection items, List<Query> queries) throws DataStoreException
    {
        if (items == null)
            return new DataObjectFeed<>();

        //Query items in table.
        Iterator<Item> iterator = items.iterator();

        if (!iterator.hasNext())
            return new DataObjectFeed();

        DataObjectFeed responseFeed = new DataObjectFeed();
        QueryPredicate queryPredicate = new QueryPredicate(queries);
        while (iterator.hasNext())
        {
            Item item = iterator.next();
            D dataObject;
            if (item == null)
                break;
            try
            {
                dataObject = (D) objectMapper.readValue(item.toJSON(), dataObjectClass);
            }
            catch (IOException e)
            {
                throw new DataStoreException(String.format("Unable to parse table entry. %s" + item.toJSON()), e);
            }
            if (logger.isDebugEnabled())
                logger.debug("Dynamo pulled item: {}", item.toJSONPretty());
            if (queryPredicate.evaluate(dataObject))
                responseFeed.add(dataObject);
        }
        return responseFeed;
    }


    private void addCondition(List<String> conditions, ValueMap valueMap, Query query)
    {
        final String awsQueryValueKey = String.format(QUERY_VALUE, query.getField());
        valueMap.withString(awsQueryValueKey, (String) query.getValue());
        conditions.add(String.format(KEY_CONDITION, query.getField(), query.getField()));
    }

    /**
     * Scan does a table scan and filters the items before sending back results. It does not work with
     * arrays so we can't filter on collections , but we can try to filter on the rest.
     * @param table DynamoDBTable to work on
     * @param queries Queries to filter results
     * @return DynamoDB ItemCollection
     * @throws IOException Unable to query DB
     */
    private ItemCollection scan(Table table, Collection<Query> queries)
    {
        List<ScanFilter> scanFilters = new ArrayList<>();

        //set what filters we can that are non array so the DB will filter prior to returning the results.
        for(Query query : queries)
        {
            if(!query.isCollection())
            {
                scanFilters.add(new ScanFilter(query.getField().name()).eq(query.getValue()));
                logger.info("DynamoDB scan condition {}", scanFilters.toString());
            }
        }
        if(scanFilters.size() == 0)
        {
            scanFilters.add(new ScanFilter("id").exists());
        }
        return table.scan(scanFilters.toArray(new ScanFilter[scanFilters.size()]));
    }

    /**
     * Query does a table query by primary key. All queries are expected to be key conditions.
     * @param table DynamoDBTable to work on
     * @param queries Queries to filter results
     * @return DynamoDB ItemCollection
     * @throws IOException Unable to query DB
     */
    private ItemCollection query(Table table, Collection<Query> queries)
    {
        QuerySpec awsQuery = new QuerySpec();
        ValueMap awsQueryValueMap = new ValueMap();

        if (queries != null)
        {
            logger.info("Performing queries");
            List<String> keyConditions = new ArrayList();
            for(Query query : queries)
                addCondition(keyConditions, awsQueryValueMap, query);

            awsQuery.withKeyConditionExpression(StringUtils.join(" AND ", keyConditions.toArray(new String[keyConditions.size()])));
            awsQuery.withValueMap(awsQueryValueMap);
            logger.info("DynamoDB key condition {}", awsQuery.getKeyConditionExpression());
            logger.info("DynamoDB value map {}", awsQuery.getValueMap() != null ? awsQuery.getValueMap().toString() : "");

            return table.query(awsQuery);
        }
        return null;
    }
}
