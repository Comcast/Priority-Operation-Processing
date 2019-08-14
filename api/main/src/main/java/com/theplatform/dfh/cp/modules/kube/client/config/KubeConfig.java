package com.theplatform.dfh.cp.modules.kube.client.config;

/**
 * Kubernetes configuration details
 */
public class KubeConfig
{
    private String masterUrl;
    private String nameSpace;
    private String certificateAuthorityPath;
    private String certificatePath;
    private String keyPath;

    private String caCertData;
    private String oauthToken;

    private String zone;

    public String getZone()
    {
        return zone;
    }

    public void setZone(String zone)
    {
        this.zone = zone;
    }

    public String getMasterUrl()
    {
        return masterUrl;
    }

    public KubeConfig setMasterUrl(String masterUrl)
    {
        this.masterUrl = masterUrl;
        return this;
    }

    public String getNameSpace()
    {
        return nameSpace;
    }

    public KubeConfig setNameSpace(String nameSpace)
    {
        this.nameSpace = nameSpace;
        return this;
    }

    public String getCertificateAuthorityPath()
    {
        return certificateAuthorityPath;
    }

    public KubeConfig setCertificateAuthorityPath(String certificateAuthorityPath)
    {
        this.certificateAuthorityPath = certificateAuthorityPath;
        return this;
    }

    public String getCertificatePath()
    {
        return certificatePath;
    }

    public KubeConfig setCertificatePath(String certificatePath)
    {
        this.certificatePath = certificatePath;
        return this;
    }

    public String getKeyPath()
    {
        return keyPath;
    }

    public KubeConfig setKeyPath(String keyPath)
    {
        this.keyPath = keyPath;
        return this;
    }

    public String getCaCertData()
    {
        return caCertData;
    }

    public KubeConfig setCaCertData(String caCertData)
    {
        this.caCertData = caCertData;
        return this;
    }

    public String getOauthToken()
    {
        return oauthToken;
    }

    public KubeConfig setOauthToken(String oauthToken)
    {
        this.oauthToken = oauthToken;
        return this;
    }
}
