package com.comcast.fission.agenda.reclaim.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.comcast.fission.agenda.reclaim.aws.config.AWSReclaimerConfig;
import com.comcast.fission.agenda.reclaim.aws.dynamo.DynamoDBTimeoutProducerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaReclaimerFactory;
import com.theplatform.dfh.cp.agenda.reclaim.factory.TimeoutConsumerFactory;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Main entry point class for a CloudWatch Event trigger
 *
 * The incoming request from an event is whatever is specified in the event (assuming constant JSON text)
 */
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AgendaReclaimerFactory agendaReclaimerFactory = new AgendaReclaimerFactory();
    private EnvironmentFacade environmentFacade = new EnvironmentFacade();
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private AWSDynamoDBFactory awsDynamoDBFactory = new AWSDynamoDBFactory();

    private static final String ENV_IDM_ENCRYPTED_PASS = "IDM_ENCRYPTED_PASS";
    private static final String ENV_IDM_USER = "IDM_USER";
    private static final String ENV_IDENTITY_URL = "IDENTITY_URL";
    private static final String ENV_AGENDA_PROGRESS_URL = "AGENDA_PROGRESS_URL";

    public AWSLambdaStreamEntry()
    {
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger, false);

        AWSReclaimerConfig reclaimerConfig;
        try
        {
            reclaimerConfig = objectMapper.readValue(inputStream, AWSReclaimerConfig.class);
        }
        catch(IOException e)
        {
            throw new BadRequestException("Failed to read input as ReclaimerConfig.", e);
        }
        if(reclaimerConfig == null)
        {
            throw new BadRequestException("Input not configured correctly. No parameters were specified.");
        }

        HttpURLConnectionFactory urlConnectionFactory = createHttpURLConnectionFactory();

        String agendaProgressURL = getEnvironmentVar(ENV_AGENDA_PROGRESS_URL);

        try
        {
            agendaReclaimerFactory.createAgendaReclaimer(
                new DynamoDBTimeoutProducerFactory(awsDynamoDBFactory, reclaimerConfig),
                new TimeoutConsumerFactory(urlConnectionFactory, reclaimerConfig, agendaProgressURL),
                reclaimerConfig
            )
            .process();
        }
        catch (Throwable t)
        {
            throw new RuntimeException("Agenda reclaim processing failed.", t);
        }
    }

    private String getEnvironmentVar(String var) throws BadRequestException
    {
        String value = environmentFacade.getEnv(var);
        if(StringUtils.isBlank(value))
        {
            throw new BadRequestException(String.format("Missing environment var: %1$s", var));
        }
        return value;
    }

    private HttpURLConnectionFactory createHttpURLConnectionFactory() throws BadRequestException
    {
        String identityUrl = getEnvironmentVar(ENV_IDENTITY_URL);
        String user = getEnvironmentVar(ENV_IDM_USER);
        String encryptedPass = environmentLookupUtils.getEncryptedVarFromEnvironment(ENV_IDM_ENCRYPTED_PASS);

        // Original used a special client
        /**
        EncryptedAuthenticationClient encryptedAuthenticationClient = new EncryptedAuthenticationClient(identityUrl, user, encryptedPass, null);

        return new IDMHTTPUrlConnectionFactory(encryptedAuthenticationClient);
        **/
        return new NoAuthHTTPUrlConnectionFactory();
    }

    public void setEnvironmentFacade(EnvironmentFacade environmentFacade)
    {
        this.environmentFacade = environmentFacade;
    }

    public void setEnvironmentLookupUtils(EnvironmentLookupUtils environmentLookupUtils)
    {
        this.environmentLookupUtils = environmentLookupUtils;
    }

    public AWSLambdaStreamEntry setAgendaReclaimerFactory(AgendaReclaimerFactory agendaReclaimerFactory)
    {
        this.agendaReclaimerFactory = agendaReclaimerFactory;
        return this;
    }
}
