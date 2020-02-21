package com.theplatform.dfh.cp.handler.kubernetes.support.payload.compression;

import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentVariableProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CompressedEnvironmentPayloadTest
{
    private final static Random random = new Random();

    private CompressedEnvironmentPayloadWriter writer;
    private CompressedEnvironmentPayloadReader reader;

    private LaunchDataWrapper mockLaunchDataWrapper;
    private EnvironmentFieldRetriever environmentRetriever;

    @BeforeMethod
    public void setup()
    {
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        environmentRetriever = new EnvironmentFieldRetriever();
        doReturn(environmentRetriever).when(mockLaunchDataWrapper).getEnvironmentRetriever();

        writer = new CompressedEnvironmentPayloadWriter();
        reader = new CompressedEnvironmentPayloadReader(mockLaunchDataWrapper);
    }

    @Test
    public void testWriteReadPayload()
    {
        final String PAYLOAD = generatePayload(512_875);
        Map<String, String> envVars = new HashMap<>();
        environmentRetriever.setEnvironmentVariableProvider(new EnvironmentVariableProvider()
        {
            @Override
            public String getVariable(String envVar)
            {
                return envVars.get(envVar);
            }
        });
        writer.writePayload(PAYLOAD, envVars);
        String resultPayload = reader.readPayload();
        Assert.assertEquals(resultPayload, PAYLOAD);
    }

    @DataProvider
    public Object[][] writePartsToMapProvider()
    {
        return new Object[][]
            {
                {100_000, 96_000, 1},
                {100_000, 99_999, 1},
                {100_000, 100_000, 1},
                {100_000, 100_001, 2},
                {100_000, 500_000, 5},
                {100_000, 500_001, 6}
            };
    }

    // focused test on just the breaking up of a string
    @Test(dataProvider = "writePartsToMapProvider")
    public void testWritePartsToMap(final int MAX_VAR_LENGTH, final int PAYLOAD_LENGTH, final int EXPECTED_PARTS)
    {
        writer.setMaxPayloadSegmentLength(MAX_VAR_LENGTH);
        final String PAYLOAD = generatePayload(PAYLOAD_LENGTH);
        Assert.assertEquals(PAYLOAD.length(), PAYLOAD_LENGTH);
        Map<String, String> payloadMap = new HashMap<>();
        writer.writePartsToMap(PAYLOAD, payloadMap);
        Assert.assertEquals(payloadMap.size(), EXPECTED_PARTS);
        StringBuilder restoredBuilder = new StringBuilder();
        for(int idx = 0; idx < EXPECTED_PARTS; idx++)
        {
            String subSection = payloadMap.get(BaseCompressedEnvironmentPayload.getEnvironmentVariableName(idx));
            int expectedLength = MAX_VAR_LENGTH;
            if(idx + 1 == EXPECTED_PARTS)
            {
                expectedLength = PAYLOAD_LENGTH % MAX_VAR_LENGTH;
                if(expectedLength == 0)
                    expectedLength = MAX_VAR_LENGTH; // perfectly matched boundaries
            }
            Assert.assertEquals(subSection.length(), expectedLength);
            restoredBuilder.append(subSection);
        }
        Assert.assertEquals(restoredBuilder.toString(), PAYLOAD);
    }

    public String generatePayload(final int LENGTH)
    {
        StringBuilder builder = new StringBuilder();
        IntStream.range(0, LENGTH).forEach(i ->
            builder.append(getRandomChar())
        );
        return builder.toString();
    }

    public char getRandomChar()
    {
        // ! -> } on the ascii char (or so)
        return (char) (random.nextInt(92) + '!');
    }
}
