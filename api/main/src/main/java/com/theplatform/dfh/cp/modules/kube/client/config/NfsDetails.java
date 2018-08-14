package com.theplatform.dfh.cp.modules.kube.client.config;

/**
 * 
 */
public class NfsDetails
{
    // The dns of the nfs server to create mounts for
    private String nfsServer;
    // The path to use from the nfs server
    private String nfsServerPath;
    // The mounts to create in relation to the nfs
    private String[] nfsMountPaths;
    // Flag indicating if the nfs volume is to be readonly
    private Boolean nfsReadOnly;                          // todo boolean or Boolean?

    public String getNfsServer()
    {
        return nfsServer;
    }

    public void setNfsServer(String nfsServer)
    {
        this.nfsServer = nfsServer;
    }

    public String getNfsServerPath()
    {
        return nfsServerPath;
    }

    public void setNfsServerPath(String nfsServerPath)
    {
        this.nfsServerPath = nfsServerPath;
    }

    public String[] getNfsMountPaths()
    {
        return nfsMountPaths;
    }

    public void setNfsMountPaths(String[] nfsMountPaths)
    {
        this.nfsMountPaths = nfsMountPaths;
    }

    public Boolean getNfsReadOnly()
    {
        return nfsReadOnly;
    }

    public void setNfsReadOnly(Boolean nfsReadOnly)
    {
        this.nfsReadOnly = nfsReadOnly;
    }
}
