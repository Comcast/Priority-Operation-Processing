package com.cts.fission.scheduling.master.aws.live;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.cts.fission.scheduling.master.aws.AWSLambdaFactory;
import com.cts.fission.scheduling.master.aws.AWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.endpoint.client.HttpObjectClientFactory;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class AWSLambdaStreamEntryLiveTest
{
    @Test(enabled = false)
    // If you get any errors about the build props just do 1 mvn clean install from the command line
    public void manualTest()
    {
        final String STAGE_ID = "dev";
        final Map<String, String> envVars = new HashMap<>();
        envVars.put(AWSLambdaStreamEntry.ENV_ENDPOINT_URL, "https://g9solclg15.execute-api.us-west-2.amazonaws.com");
        envVars.put(AWSLambdaStreamEntry.ENV_RESOURCEPOOL_ENDPOINT_PATH, "/fission/resourcepool");
        envVars.put(AWSLambdaStreamEntry.ENV_RESOURCEPOOL_LAMBDA_LAUNCH_LIST, "dfh-fission-twinkle-SchedulingQueue-HIDXPAS4J9VX");

        EnvironmentFacade environmentFacade = new EnvironmentFacade()
        {
            @Override
            public String getEnv(String var)
            {
                return envVars.get(var);
            }
        };
        EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils()
        {
            @Override
            public String getEncryptedVarFromEnvironment(String varName)
            {
                return envVars.get(varName);
            }
        };

        String requestJson = ("{\"stageId\":\""+ STAGE_ID +"\"}");

        new AWSLambdaStreamEntry(
                new AWSLambdaFactory(new ProfileCredentialsProvider("lab_Fission")).setRegion(Regions.US_WEST_2),
                new HttpObjectClientFactory(new NoAuthHTTPUrlConnectionFactory())
            )
            .setEnvironmentFacade(environmentFacade)
            .setEnvironmentLookupUtils(environmentLookupUtils)
            .handleRequest(
                new ByteArrayInputStream(requestJson.getBytes()),
                mock(OutputStream.class),
                mock(Context.class)
            );
    }
}
