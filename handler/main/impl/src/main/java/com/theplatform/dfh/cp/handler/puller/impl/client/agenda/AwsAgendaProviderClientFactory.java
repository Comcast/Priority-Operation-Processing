package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.module.authentication.client.EncryptedAuthenticationClient;


public class AwsAgendaProviderClientFactory implements AgendaClientFactory
{
    private String identityUrl;
    private String username;
    private String encryptedPassword;
    private String endpointUrl;

    public AwsAgendaProviderClientFactory(PullerConfig pullerConfig)
    {
        this.endpointUrl = pullerConfig.getAgendaProviderUrl();
        this.identityUrl = pullerConfig.getIdentityUrl();
        this.username = pullerConfig.getUsername();
        this.encryptedPassword = pullerConfig.getEncryptedPassword();
    }

    public AwsAgendaProviderClient getClient()
    {
        EncryptedAuthenticationClient authClient = new EncryptedAuthenticationClient(identityUrl, username, encryptedPassword, null);

        HttpURLConnectionFactory httpURLConnectionFactory = new IDMHTTPUrlConnectionFactory(authClient);

        return new AwsAgendaProviderClient(endpointUrl, httpURLConnectionFactory);
    }
}
