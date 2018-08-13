package com.theplatform.dfh.cp.kube.client.config;

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

    public String getMasterUrl()
    {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl)
    {
        this.masterUrl = masterUrl;
    }

    public String getNameSpace()
    {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace)
    {
        this.nameSpace = nameSpace;
    }

    public String getCertificateAuthorityPath()
    {
        return certificateAuthorityPath;
    }

    public void setCertificateAuthorityPath(String certificateAuthorityPath)
    {
        this.certificateAuthorityPath = certificateAuthorityPath;
    }

    public String getCertificatePath()
    {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath)
    {
        this.certificatePath = certificatePath;
    }

    public String getKeyPath()
    {
        return keyPath;
    }

    public void setKeyPath(String keyPath)
    {
        this.keyPath = keyPath;
    }

    public String getCaCertData()
    {
        return caCertData;
    }

    public void setCaCertData(String caCertData)
    {
        this.caCertData = caCertData;
    }

    public String getOauthToken()
    {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken)
    {
        this.oauthToken = oauthToken;
    }
}
