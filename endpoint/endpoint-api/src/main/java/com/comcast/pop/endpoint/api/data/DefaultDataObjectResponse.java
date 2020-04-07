package com.comcast.pop.endpoint.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.comcast.pop.endpoint.api.DefaultServiceResponse;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class DefaultDataObjectResponse<D extends IdentifiedObject> extends DefaultServiceResponse implements DataObjectResponse<D>
{
    public DefaultDataObjectResponse()
    {}

    public DefaultDataObjectResponse(ErrorResponse errorResponse)
    {
        setErrorResponse(errorResponse);
    }

    private Integer count;
    private List<D> dataObjects = new ArrayList<>();

    public void add(D dataObject)
    {
        dataObjects.add(dataObject);
    }

    public void addAll(List<D> dataObjects)
    {
        this.dataObjects.addAll(dataObjects);
    }

    public List<D> getAll()
    {
        return dataObjects;
    }

    public Integer getCount()
    {
        return count == null && dataObjects != null ? dataObjects.size() : count;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }

    @JsonIgnore
    public D getFirst()
    {
        if(dataObjects == null || dataObjects.size() == 0) return null;
        return dataObjects.get(0);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DefaultDataObjectResponse that = (DefaultDataObjectResponse) o;

        if (dataObjects != null
                ? !dataObjects.equals(that.dataObjects)
                : that.dataObjects != null)
            return false;
        return getErrorResponse() != null
                ? getErrorResponse().equals(that.getErrorResponse())
                : that.getErrorResponse() == null;
    }

    @Override
    public int hashCode()
    {
        int result = dataObjects != null
                ? dataObjects.hashCode()
                : 0;
        result = 31 * result + (getErrorResponse() != null
                ? getErrorResponse().hashCode()
                : 0);
        return result;
    }
}

