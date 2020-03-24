package com.cts.fission.scheduling.master.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.endpoint.client.HttpObjectClientFactory;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main entry point class for a CloudWatch Event trigger
 *
 * The incoming request from an event is whatever is specified in the event (assuming constant JSON text)
 */
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ENV_IDM_ENCRYPTED_PASS = "IDM_ENCRYPTED_PASS";
    public static final String ENV_IDM_USER = "IDM_USER";
    public static final String ENV_IDENTITY_URL = "IDENTITY_URL";
    public static final String ENV_ENDPOINT_URL = "ENDPOINT_URL";
    public static final String ENV_RESOURCEPOOL_ENDPOINT_PATH = "RESOURCEPOOL_ENDPOINT_PATH";
    public static final String ENV_RESOURCEPOOL_LAMBDA_LAUNCH_LIST = "RESOURCEPOOL_LAMBDA_LAUNCH_LIST";

    private EnvironmentFacade environmentFacade = new EnvironmentFacade();
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private HttpObjectClientFactory objectClientFactory;
    private AWSLambdaFactory awsLambdaFactory;

    public AWSLambdaStreamEntry(AWSLambdaFactory awsLambdaFactory, HttpObjectClientFactory objectClientFactory)
    {
        this.awsLambdaFactory = awsLambdaFactory;
        this.objectClientFactory = objectClientFactory;
    }

    public AWSLambdaStreamEntry()
    {
        awsLambdaFactory = new AWSLambdaFactory();
        objectClientFactory = new HttpObjectClientFactory(getHttpURLConnectionFactory());
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);

        SchedulerRequest request;
        try
        {
            request = objectMapper.readValue(inputStream, SchedulerRequest.class);
        }
        catch(IOException e)
        {
            throw new BadRequestException(String.format("Failed to read input as %1$s", SchedulerRequest.class.getSimpleName()), e);
        }
        if(request == null || request.getStageId() == null)
        {
            throw new BadRequestException("Request must have a stageId.");
        }
        processAllResourcePools(request);
    }

    protected void processAllResourcePools(SchedulerRequest request)
    {
        String endpointURL = getEnvironmentVar(ENV_ENDPOINT_URL);
        String rawLaunchLambdaList = getEnvironmentVar(ENV_RESOURCEPOOL_LAMBDA_LAUNCH_LIST);
        String resourcePoolEndpointPath = getEnvironmentVar(ENV_RESOURCEPOOL_ENDPOINT_PATH);

        HttpObjectClient<ResourcePool> resourcePoolClient = objectClientFactory.createClient(
            environmentLookupUtils.getAPIEndpointURL(endpointURL, request.getStageId(), resourcePoolEndpointPath),
            ResourcePool.class);

        List<String> lambdasToLaunch = Arrays.stream(StringUtils.split(rawLaunchLambdaList, ","))
            .map(String::trim).collect(Collectors.toList());

        try
        {
            DataObjectResponse<ResourcePool> response = resourcePoolClient.getObjects(new ArrayList<>());
            if(response.isError())
            {
                logger.error("Failed to get all ResourcePools: {}", response.getErrorResponse().getServerStackTrace());
                return;
            }
            response.getAll().forEach( resourcePool ->
                launchResourcePoolLambda(lambdasToLaunch, resourcePool, request)
            );
        }
        catch (Throwable t)
        {
            throw new RuntimeException("Error processing resource pools.", t);
        }
    }

    private void launchResourcePoolLambda(List<String> lambdasToLaunch, ResourcePool resourcePool, SchedulerRequest schedulerRequest)
    {
        lambdasToLaunch.forEach(lambdaName ->
        {
            logger.info("Launching Scheduled ResourcePool Lambda {} for: {}:{}", lambdaName, resourcePool.getId(), resourcePool.getTitle());
            AWSLambda awsLambda = awsLambdaFactory.create();
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName(String.format("%1$s:%2$s", lambdaName, schedulerRequest.getStageId()));
            // run as an event (headless)
            invokeRequest.setInvocationType(InvocationType.Event);
            invokeRequest.setPayload(createPayload(resourcePool.getId(), schedulerRequest.getStageId()));
            awsLambda.invoke(invokeRequest);
        });
    }

    private String createPayload(String resourcePoolId, String stageId)
    {
        // TODO: if desired/necessary someone can create an entire api module with 1 class to share between this and
        // the resource pool scheduler lambda

        return "{" +
            "\"resourcePoolId\":" +
            "\"" + resourcePoolId + "\"" +
            ", \"stageId\":" +
            "\"" + stageId + "\"" +
            "}";
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

    private HttpURLConnectionFactory getHttpURLConnectionFactory() throws BadRequestException
    {
        String identityUrl = getEnvironmentVar(ENV_IDENTITY_URL);
        String user = getEnvironmentVar(ENV_IDM_USER);
        String encryptedPass = environmentLookupUtils.getEncryptedVarFromEnvironment(ENV_IDM_ENCRYPTED_PASS);

        /**
        EncryptedAuthenticationClient encryptedAuthenticationClient = new EncryptedAuthenticationClient(identityUrl, user, encryptedPass, null);

        return new IDMHTTPUrlConnectionFactory(encryptedAuthenticationClient);
         **/
        return new NoAuthHTTPUrlConnectionFactory();
    }

    public AWSLambdaStreamEntry setAwsLambdaFactory(AWSLambdaFactory awsLambdaFactory)
    {
        this.awsLambdaFactory = awsLambdaFactory;
        return this;
    }

    public AWSLambdaStreamEntry setEnvironmentFacade(EnvironmentFacade environmentFacade)
    {
        this.environmentFacade = environmentFacade;
        return this;
    }

    public AWSLambdaStreamEntry setEnvironmentLookupUtils(EnvironmentLookupUtils environmentLookupUtils)
    {
        this.environmentLookupUtils = environmentLookupUtils;
        return this;
    }

}
