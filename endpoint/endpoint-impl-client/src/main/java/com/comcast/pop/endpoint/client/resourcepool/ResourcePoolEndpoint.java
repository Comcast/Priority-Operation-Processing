package com.comcast.pop.endpoint.client.resourcepool;

public enum ResourcePoolEndpoint
{
    getAgenda("getAgenda"),
    createAgenda("createAgenda"),
    updateAgendaProgress("updateAgendaProgress"),
    updateAgenda("updateAgenda");

    private String path;

    ResourcePoolEndpoint(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
}
