package com.comcast.pop.api;

import com.comcast.pop.api.params.ParamsMap;

public interface FileResource
{
    public int getIndex();
    public void setIndex(int index);
    public String getLabel();
    public void setLabel(String label);
    public ParamsMap getCredentials();
    public void setCredentials(ParamsMap credentials);
    public String getType();
    public void setType(String type);
    public String getUrl();
    public void setUrl(String url);
    public ParamsMap getParams();
    public void addParam(String name, Object value);
}
