package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.persistence.api.PersistentObjectConverter;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * T = api object class
 * S = persistent object class
 */
public class DynamoDBPersistentObjectConverter<T, S>  implements PersistentObjectConverter<T, S>
{
    private static final Logger logger = LoggerFactory.getLogger(DynamoDBPersistentObjectConverter.class);

    Class dataClazz;
    Class persistentClazz;

    public DynamoDBPersistentObjectConverter(Class dataClazz, Class persistentClazz)
    {
        this.dataClazz = dataClazz;
        this.persistentClazz = persistentClazz;
    }

    public Class getPersistentObjectClass()
    {
        return persistentClazz;
    }

    public Class getDataObjectClass()
    {
        return dataClazz;
    }

    public S getPersistentObject(T dataObject)
    {
        if (dataObject == null)
            return null;

        S persistentObject = (S) getNewIntance(persistentClazz);
        return copy(persistentObject, dataObject) ? persistentObject : null;
    }

    public T getDataObject(S persistentObject)
    {
        if (persistentObject == null)
            return null;

        T dataObject = (T) getNewIntance(dataClazz);
        return copy(dataObject, persistentObject) ? dataObject : null;
    }

    public boolean copy(Object dest, Object orig)
    {
        try
        {
            BeanUtils.copyProperties(dest, orig);
            return true;
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            logger.error("Unable to copy properties from data object to persistence", e);
            return false;
        }
    }

    Object getNewIntance(Class clazz)
    {
        try
        {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e)
        {
            logger.debug("");
            return null;
        }
    }
}
