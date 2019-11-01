package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.params.ParamsMap;

public class AgendaTemplate extends DefaultEndpointDataObject
{
    private String title;
    private ParamsMap templateParameters;
    private ParamsMap staticParameters;
    private Agenda agenda;
    private boolean defaultTemplate;
    private boolean globalTemplate;

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

    public boolean isGlobalTemplate()
    {
        return globalTemplate;
    }

    public void setGlobalTemplate(boolean globalTemplate)
    {
        this.globalTemplate = globalTemplate;
    }
}
