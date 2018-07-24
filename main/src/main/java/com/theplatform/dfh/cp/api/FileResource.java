package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.params.ParamsMap;

public interface FileResource
{
    public int getIndex();
    public void setIndex(int index);
    public String getLabel();
    public void setLabel(String label);
    public Credentials getCredentials();
    public void setCredentials(Credentials credentials);
    public String getType();
    public void setType(String type);
    public String getUrl();
    public void setUrl(String url);
    public ParamsMap getParams();

}
