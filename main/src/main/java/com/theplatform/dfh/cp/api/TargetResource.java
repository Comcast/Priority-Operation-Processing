package com.theplatform.dfh.cp.api;

import com.fasterxml.jackson.annotation.JsonProperty;


public class TargetResource implements FileResource
{
    private int index;
    private String type = FileResourceType.unknown.name();
    private String url;
    private String label;
    private String username;
    private String password;
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
    public String getUsername()
    {
        return username;
    }

    @JsonProperty
    public void setUsername(String username)
    {
        this.username = username;
    }

    @JsonProperty
    public String getPassword()
    {
        return password;
    }

    @JsonProperty
    public void setPassword(String password)
    {
        this.password = password;
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
