package com.theplatform.dfh.persistence.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataObjectFeed<D> implements Serializable
{
    public static final long serialVersionUID = 4220502009345987828L;

    private List<D> dataObjects = new ArrayList<>();
    private PersistenceException exception;
    
    public PersistenceException getException()
    {
        return exception;
    }

    public void setException(PersistenceException exception)
    {
        this.exception = exception;
    }

    public boolean isError()
    {
        return exception != null;
    }

    public void add(D workType)
    {
        dataObjects.add(workType);
    }
    public List<D> getAll()
    {
        return dataObjects;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataObjectFeed that = (DataObjectFeed) o;

        if (dataObjects != null
                ? !dataObjects.equals(that.dataObjects)
                : that.dataObjects != null)
            return false;
        return getException() != null
                ? getException().equals(that.getException())
                : that.getException() == null;
    }

    @Override
    public int hashCode()
    {
        int result = dataObjects != null
                ? dataObjects.hashCode()
                : 0;
        result = 31 * result + (getException() != null
                ? getException().hashCode()
                : 0);
        return result;
    }
}
