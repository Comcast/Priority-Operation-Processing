package com.theplatform.dfh.cp.kube.fabric8.client.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OAuthCredentialCapture
{
    private static Logger logger = LoggerFactory.getLogger(OAuthCredentialCapture.class);

    public static final String OAUTH_CERT = "OATH_CERT";
    public static final String OAUTH_TOKEN = "OATH_TOKEN";
    private String oauthCert;
    private String oauthToken;
    private boolean oAuthAvailable;

    public OAuthCredentialCapture init()
    {
        oauthCert = System.getenv(OAUTH_CERT);
        oauthToken = System.getenv(OAUTH_TOKEN);
        if (oauthCert != null && oauthToken != null)
        {
            oAuthAvailable = true;
        }
        if (isOAuthAvailable())
        {
            logger.debug("{} has length: {}", OAUTH_CERT, oauthCert.length());
            logger.debug("{} has length: {}", OAUTH_TOKEN, oauthToken.length());
        }
        else
        {
            logger.debug("OAUTH is not available.");
        }


        return this;
    }

    public String getOauthCert()
    {
        return oauthCert;
    }

    public String getOauthToken()
    {
        return oauthToken;
    }

    public boolean isOAuthAvailable()
    {
        return oAuthAvailable;
    }

    protected void setOAuthAvailable(boolean oAuthAvailable)
    {
        this.oAuthAvailable = oAuthAvailable;
    }
}