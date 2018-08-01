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

    @JsonProperty
    public String getLabel()
    {
        return label;
    }

    @JsonProperty
    public void setLabel(String label)
    {
        this.label = label;
    }

    @JsonProperty
    public ParamsMap getCredentials()
    {
        return credentials;
    }

    @JsonProperty
    public void setCredentials(ParamsMap credentials)
    {
        this.credentials = credentials;
    }

    @JsonProperty
    public String getType()
    {
        return type;
    }

    @JsonProperty
    public void setType(String type)
    {
        this.type = type;
    }

    @JsonProperty
    public String getUrl()
    {
        return url;
    }

    @JsonProperty
    public void setUrl(String url)
    {
        this.url = url;
    }

    @JsonProperty
    public ParamsMap getParams()
    {
        return params;
    }

}
