package com.theplatform.dfh.cp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.params.ParamsMap;


public class AbstractFileResource implements FileResource
{
    private int index;
    private String type = FileResourceType.UNKNOWN.name();
    private String url;
    private String label;
    private ParamsMap credentials = new ParamsMap();
    private ParamsMap params = new ParamsMap();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public int getIndex()
    {
        return index;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setIndex(int index)
    {
        this.index = index;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public ParamsMap getCredentials()
    {
        return credentials;
    }

    public void setCredentials(ParamsMap credentials)
    {
        this.credentials = credentials;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public ParamsMap getParams()
    {
        return params;
    }

    public void addParam(String name, Object value)
    {
        params.put(name, value);
    }
}
