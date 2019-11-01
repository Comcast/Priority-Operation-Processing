package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.params.ParamsMap;

public class AgendaTemplate extends AllowedCustomerEndpointDataObject implements GlobalEndpointDataObject
{
    private String title;
    private ParamsMap templateParameters;
    private ParamsMap staticParameters;
    private ParamsMap params;
    private Agenda agenda;
    private boolean defaultTemplate;
    private boolean isGlobal = false;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public ParamsMap getTemplateParameters()
    {
        return templateParameters;
    }

    public void setTemplateParameters(ParamsMap templateParameters)
    {
        this.templateParameters = templateParameters;
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

    public boolean isDefaultTemplate()
    {
        return defaultTemplate;
    }

    public void setDefaultTemplate(boolean defaultTemplate)
    {
        this.defaultTemplate = defaultTemplate;
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }
    public boolean getIsGlobal()
    {
        return isGlobal;
    }
    public void setIsGlobal(boolean global)
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
