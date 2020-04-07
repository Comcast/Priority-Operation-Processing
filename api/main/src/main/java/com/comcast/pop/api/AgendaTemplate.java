package com.comcast.pop.api;

import com.comcast.pop.api.params.ParamsMap;

public class AgendaTemplate extends AllowedCustomerEndpointDataObject implements GlobalEndpointDataObject
{
    private String title;
    private ParamsMap staticParameters;
    private ParamsMap params;
    private Agenda agenda;
    private Boolean isDefaultTemplate;
    private Boolean isGlobal;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public ParamsMap getStaticParameters()
    {
        return staticParameters;
    }

    public void setStaticParameters(ParamsMap staticParameters)
    {
        this.staticParameters = staticParameters;
    }

    public Agenda getAgenda()
    {
        return agenda;
    }

    public void setAgenda(Agenda agenda)
    {
        this.agenda = agenda;
    }

    public Boolean getIsDefaultTemplate()
    {
        return isDefaultTemplate;
    }

    public void setIsDefaultTemplate(Boolean defaultTemplate)
    {
        isDefaultTemplate = defaultTemplate;
    }

    public Boolean isGlobal()
    {
        return isGlobal;
    }
    public Boolean getIsGlobal()
    {
        return isGlobal;
    }
    public void setIsGlobal(Boolean global)
    {
        isGlobal = global;
    }

    public ParamsMap getParams()
    {
        return params;
    }

    public void setParams(ParamsMap params)
    {
        this.params = params;
    }
}
