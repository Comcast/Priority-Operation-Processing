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
    private Boolean nfsReadOnly;

    public String getNfsServer()
    {
        return nfsServer;
    }

    public NfsDetails setNfsServer(String nfsServer)
    {
        this.nfsServer = nfsServer;
        return this;
    }

    public String getNfsServerPath()
    {
        return nfsServerPath;
    }

    public NfsDetails setNfsServerPath(String nfsServerPath)
    {
        this.nfsServerPath = nfsServerPath;
        return this;
    }

    public String[] getNfsMountPaths()
    {
        return nfsMountPaths;
    }

    public NfsDetails setNfsMountPaths(String[] nfsMountPaths)
    {
        this.nfsMountPaths = nfsMountPaths;
        return this;
    }

    public Boolean getNfsReadOnly()
    {
        return nfsReadOnly;
    }

    public NfsDetails setNfsReadOnly(Boolean nfsReadOnly)
    {
        this.nfsReadOnly = nfsReadOnly;
        return this;
    }
}
