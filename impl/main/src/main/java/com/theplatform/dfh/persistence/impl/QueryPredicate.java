package com.theplatform.dfh.persistence.impl;

import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.Predicate;
/**
 * This class compares Query requests to a dataobject for filtering.
 * If Query.field = slotcount, then it will compare the slot count in the query
 * request to the slot count of the work type object.
 * If all query filters match it will evaluate as true.
 */
public class QueryPredicate<D> implements Predicate<D>
{
    private static final Logger logger = LoggerFactory.getLogger(QueryPredicate.class);
    private Collection<Query> queries;

    public QueryPredicate(
        Collection<Query> queries)
    {
        this.queries = queries;
    }

    @Override
    public boolean evaluate(D object)
    {
        if(object == null) return queries == null;
        if(queries == null) return true;
        try
        {
            //loop through query filters and filter out non matching values.
            for(Query query : queries)
            {
                Object valueForQueryField = PropertyUtils.getProperty(object, query.getField().name());
                Collection valuesForQueryField = valueForQueryField instanceof Collection ? (Collection) valueForQueryField :
                                                 Collections.singleton(valueForQueryField);
                Collection queryValues = query.isCollection() ? (Collection) query.getValue() : Collections.singleton(query.getValue());

                if(!evaluateCollectionContains(valuesForQueryField, queryValues))
                {
                    return false;
                }
            }
        }
        catch (IllegalAccessException| InvocationTargetException |NoSuchMethodException e)
        {
            //this is a bug, our query field names should match the data object field names.
            logger.error("The compare function for the data object properties and the field names did not match.", e);
            return false;
        }
        return true;
    }

    /**
     * Collection evaluation checks that all the entries in the queryValue are present in the data object value.
     * This is NOT a direct equality check.
     * @param dataObjectValues The collection to check for the presence of the queryValue collection entries
     * @param queryValues The collection of entries that must be present in the dataObjectValue
     * @return true if the required entries are contained in the dataObjectValue collection, otherwise false
     */
    protected static boolean evaluateCollectionContains(Collection dataObjectValues, Collection queryValues)
    {
        // the dataObjectValue check passes because the query value is nothing
        if(dataObjectValues == null) return queryValues == null;
        if(queryValues == null) return true;
        return dataObjectValues.containsAll(queryValues);
    }
}

