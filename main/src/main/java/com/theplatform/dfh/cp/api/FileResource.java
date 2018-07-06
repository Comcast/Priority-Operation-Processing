package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.params.ParamsMap;

public interface FileResource
{
    public int getIndex();
    public void setIndex(int index);
    public String getLabel();
    public void setLabel(String label);
    public String getUsername();
    public void setUsername(String username);
    public String getPassword();
    public void setPassword(String password);
    public String getType();
    public void setType(String type);
    public String getUrl();
    public void setUrl(String url);
    public ParamsMap getParams();

}
