package com.cts.fission.scheduling.monitor.aws.live;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.cts.fission.scheduling.monitor.aws.AWSLambdaStreamEntry;
import com.cts.fission.scheduling.monitor.aws.ResourcePoolMonitorRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.modules.monitor.graphite.GraphiteConfigKeys;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.client.HttpObjectClientFactory;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.LocalDynamoDBFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class AWSLambdaStreamEntryLiveTest
{
    private static final String SCHEDULER_PASSWORD = "<DFH Scheduling MPX User PASS HERE>";

    final String AWS_PROFILE_NAME = "lab_DFH";
    final Regions AWS_REGION = Regions.US_WEST_2;

    private static final String STAGE_ID = "dev";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private AWSLambdaStreamEntry streamEntry;

    @BeforeMethod
    public void setup()
    {
        Map<String, String> envVars = new HashMap<>();
        envVars.put(GraphiteConfigKeys.ENDPOINT.getPropertyKey(), "bg");
        envVars.put(GraphiteConfigKeys.PATH.getPropertyKey(), "1m.lab.main.t.aort.dfh.testing");
//        envVars.put(AWSLambdaStreamEntry.ENV_IDM_ENCRYPTED_PASS, AES.encrypt(SCHEDULER_PASSWORD));
        envVars.put(AWSLambdaStreamEntry.ENV_IDM_USER, "service/dfh-scheduler@comcast.com");
        envVars.put(AWSLambdaStreamEntry.ENV_ENDPOINT_URL, "https://lambda.execute-api.us-west-2.amazonaws.com");
        envVars.put(AWSLambdaStreamEntry.ENV_RESOURCEPOOL_ENDPOINT_PATH, "/dfh/idm/resourcepool");
        envVars.put(AWSLambdaStreamEntry.ENV_INSIGHT_ENDPOINT_PATH, "/dfh/idm/insight");
        envVars.put(AWSLambdaStreamEntry.ENV_READY_AGENDA_TABLE, "DFH-Fission-Twinkle-ReadyAgenda");

        streamEntry = new AWSLambdaStreamEntry(
            new DynamoDBPersisterFactory<>("id", ReadyAgenda.class, AWSLambdaStreamEntry.READY_AGENDA_TABLE_INDEXES,
                new LocalDynamoDBFactory(AWS_PROFILE_NAME, AWS_REGION)),
            new HttpObjectClientFactory(
                new NoAuthHTTPUrlConnectionFactory(
            )));
        streamEntry.setEnvironmentFacade(new EnvironmentFacade()
        {
            @Override
            public String getEnv(String var)
            {
                return envVars.get(var);
            }

            @Override
            public Map<String, String> getEnv()
            {
                return envVars;
            }
        });
        streamEntry.setEnvironmentLookupUtils(new EnvironmentLookupUtils()
        {
            @Override
            public String getEncryptedVarFromEnvironment(String varName)
            {
                return envVars.get(varName);
            }

        });
    }

    @Test(enabled = false)
    public void testMetrics() throws Exception
    {
        ResourcePoolMonitorRequest resourcePoolMonitorRequest = new ResourcePoolMonitorRequest();
        resourcePoolMonitorRequest.setStageId(STAGE_ID);
        final String jsonInput = objectMapper.writeValueAsString(resourcePoolMonitorRequest);

        streamEntry.handleRequest(
            new ByteArrayInputStream(jsonInput.getBytes()),
            mock(OutputStream.class),
            mock(Context.class)
        );
    }
}
