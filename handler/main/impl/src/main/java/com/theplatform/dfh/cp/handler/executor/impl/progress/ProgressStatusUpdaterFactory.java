package com.theplatform.dfh.cp.handler.executor.impl.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.util.http.impl.exception.HttpRequestHandlerException;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.module.authentication.client.EncryptedAuthenticationClient;

public class ProgressStatusUpdaterFactory
{
    public static final String IDM_URL_FIELD = "idm.url";
    public static final String IDM_USER = "idm.service.user.name";
    public static final String IDM_ENCRYPTED_PASS = "idm.service.user.encryptedpass";
    public static final String JOB_PROGRESS_URL = "job.progress.url";

    private final String jobProgressUrl;
    private final HttpURLConnectionFactory httpURLConnectionFactory;

    public ProgressStatusUpdaterFactory(LaunchDataWrapper launchDataWrapper)
    {
        this.httpURLConnectionFactory = createIDMHTTPUrlConnectionFactory(launchDataWrapper.getPropertyRetriever());
        this.jobProgressUrl = launchDataWrapper.getPropertyRetriever().getField(JOB_PROGRESS_URL);
        if(jobProgressUrl == null)
        {
            throw new HttpRequestHandlerException("Invalid JobProgress url.");
        }
    }

    public ProgressStatusUpdater createProgressStatusUpdater(Agenda agenda)
    {
        return new ProgressStatusUpdater(jobProgressUrl, httpURLConnectionFactory, agenda);
    }

    protected static IDMHTTPUrlConnectionFactory createIDMHTTPUrlConnectionFactory(FieldRetriever fieldRetriever)
    {
        String identityUrl = fieldRetriever.getField(IDM_URL_FIELD);
        String user = fieldRetriever.getField(IDM_USER);
        String encryptedPass = fieldRetriever.getField(IDM_ENCRYPTED_PASS);
        if(identityUrl == null || user == null || encryptedPass == null)
        {
            throw new HttpRequestHandlerException("Invalid IDM credentials configured for token generation.");
        }

        return new IDMHTTPUrlConnectionFactory(new EncryptedAuthenticationClient(
            identityUrl,
            user,
            encryptedPass,
            null
        ));
    }
}
