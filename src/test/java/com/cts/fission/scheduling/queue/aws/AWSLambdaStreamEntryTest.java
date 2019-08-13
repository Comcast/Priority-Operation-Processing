package com.cts.fission.scheduling.queue.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.endpoint.client.HttpObjectClientFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AWSLambdaStreamEntryTest
{
    private final String ENCRYPTED_PASS = "rkHt4ZZh+eprk0Vv+Q035g=="; // AES 'test'

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private EnvironmentLookupUtils mockEnvironmentLookupUtils;
    private EnvironmentFacade mockEnvironmentFacade;
    private HttpObjectClientFactory mockObjectClientFactory;
    private HttpObjectClient<ResourcePool> mockResourcePoolClient;
    private AWSLambdaFactory mockAWSLambdaFactory;
    private AWSLambdaStreamEntry streamEntry;
    private Map<String, String> environmentMap;

    @BeforeMethod
    public void setup()
    {
        mockEnvironmentLookupUtils = mock(EnvironmentLookupUtils.class);
        mockEnvironmentFacade = mock(EnvironmentFacade.class);
        mockObjectClientFactory = mock(HttpObjectClientFactory.class);
        mockResourcePoolClient = mock(HttpObjectClient.class);
        mockAWSLambdaFactory = mock(AWSLambdaFactory.class);

        Map<String, String> envVars = new HashMap<>();
        doReturn(envVars).when(mockEnvironmentFacade).getEnv();
        doReturn(mock(AWSLambda.class)).when(mockAWSLambdaFactory).create();

        streamEntry = new AWSLambdaStreamEntry(mockAWSLambdaFactory, mockObjectClientFactory);
        streamEntry.setEnvironmentFacade(mockEnvironmentFacade);
        streamEntry.setEnvironmentLookupUtils(mockEnvironmentLookupUtils);
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Failed to read input as SchedulerRequest.*")
    public void testHandleRequestInvalidInput() throws IOException
    {
        // run some bad json
        callHandleRequest("{\"");
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Request must have a stageId.*")
    public void testHandleRequestNoInput() throws IOException
    {
        // run some empty json
        callHandleRequest("{}");
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Missing environment var.*")
    public void testInvalidEnvVar() throws Exception
    {
        callHandleRequest(objectMapper.writeValueAsString(new SchedulerRequest().setStageId("b")));
    }

    @DataProvider
    public Object[][] missingEnvVarProvider()
    {
        return new Object[][]
            {
                {AWSLambdaStreamEntry.ENV_ENDPOINT_URL},
                {AWSLambdaStreamEntry.ENV_RESOURCEPOOL_SCHEDULER_LAMBDA_NAME},
                {AWSLambdaStreamEntry.ENV_RESOURCEPOOL_ENDPOINT_PATH},
            };
    }

    @Test(expectedExceptions = BadRequestException.class,
        expectedExceptionsMessageRegExp = ".*Missing environment var.*",
        dataProvider = "missingEnvVarProvider"
    )
    public void testProcessAllResourcePoolsMissingEnvVar(String missingVar)
    {
        setupValidEnvironment();
        environmentMap.remove(missingVar);
        streamEntry.processAllResourcePools(new SchedulerRequest());
    }

    @DataProvider
    public Object[][] resourcePoolCountProvider()
    {
        return new Object[][]
            {
                {0},
                {1},
                {10},
            };
    }

    @Test(dataProvider = "resourcePoolCountProvider")
    public void testProcessAllResourcePools(int count)
    {
        setupValidEnvironment();
        doReturn(mockResourcePoolClient).when(mockObjectClientFactory).createClient(anyString(), eq(ResourcePool.class));
        doReturn(createResponse(count, false)).when(mockResourcePoolClient).getObjects(anyList());
        streamEntry.processAllResourcePools(new SchedulerRequest());
        verify(mockAWSLambdaFactory, times(count)).create();
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Error processing resource pools.*")
    public void testProcessAllResourcePoolsLambdaError()
    {
        setupValidEnvironment();
        AWSLambda mockAwsLambda = mock(AWSLambda.class);
        doReturn(mockAwsLambda).when(mockAWSLambdaFactory).create();
        doReturn(mockResourcePoolClient).when(mockObjectClientFactory).createClient(anyString(), eq(ResourcePool.class));
        doReturn(createResponse(1, false)).when(mockResourcePoolClient).getObjects(anyList());
        doThrow(new RuntimeException()).when(mockAwsLambda).invoke(any());
        streamEntry.processAllResourcePools(new SchedulerRequest());
        verify(mockAWSLambdaFactory, times(1)).create();
    }

    @Test
    public void testProcessAllResourcePoolsResponseError()
    {
        setupValidEnvironment();
        doReturn(mockResourcePoolClient).when(mockObjectClientFactory).createClient(anyString(), eq(ResourcePool.class));
        doReturn(createResponse(0, true)).when(mockResourcePoolClient).getObjects(anyList());
        streamEntry.processAllResourcePools(new SchedulerRequest());
        verify(mockAWSLambdaFactory, times(0)).create();
    }

    private DataObjectResponse<ResourcePool> createResponse(int entryCount, boolean error)
    {
        DataObjectResponse<ResourcePool> dataObjectResponse = new DefaultDataObjectResponse<>();
        if(error) dataObjectResponse.setErrorResponse(new ErrorResponse());
        IntStream.range(0, entryCount).forEach(i -> dataObjectResponse.add(new ResourcePool()));
        return dataObjectResponse;
    }

    private void setupValidEnvironment()
    {
        environmentMap = new HashMap<>();
        environmentMap.put(AWSLambdaStreamEntry.ENV_IDM_USER, "user@theplatform.com");
        environmentMap.put(AWSLambdaStreamEntry.ENV_IDM_ENCRYPTED_PASS, ENCRYPTED_PASS);
        environmentMap.put(AWSLambdaStreamEntry.ENV_IDENTITY_URL, "http://identity.auth.test.corp.theplatform.com/idm");
        environmentMap.put(AWSLambdaStreamEntry.ENV_ENDPOINT_URL, "https://g9solclg15.execute-api.us-west-2.amazonaws.com");
        environmentMap.put(AWSLambdaStreamEntry.ENV_RESOURCEPOOL_ENDPOINT_PATH, "/dfh/idm/resourcepool");
        environmentMap.put(AWSLambdaStreamEntry.ENV_RESOURCEPOOL_SCHEDULER_LAMBDA_NAME, "dfh-fission-twinkle-SchedulingQueue-HIDXPAS4J9VX");

        streamEntry.setEnvironmentLookupUtils(new EnvironmentLookupUtils()
        {
            @Override
            public String getEncryptedVarFromEnvironment(String varName)
            {
                return environmentMap.get(varName);
            }
        });

        streamEntry.setEnvironmentFacade(new EnvironmentFacade()
        {
            @Override
            public String getEnv(String var)
            {
                return environmentMap.get(var);
            }
        });
    }

    private void callHandleRequest(String input) throws IOException
    {
        streamEntry.handleRequest(
            new ByteArrayInputStream(input.getBytes()),
            mock(OutputStream.class),
            mock(Context.class)
        );
    }
}
